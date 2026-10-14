package com.mindflow.nova.ui.screens.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mindflow.nova.AppServices
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.local.localOrNull
import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.mission.RemoteMissionRepository
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.offline.AttemptRecorder
import com.mindflow.nova.data.offline.FinishedAttempt
import com.mindflow.nova.data.offline.LessonOutcome
import com.mindflow.nova.data.offline.MAX_PAUSE_SECONDS
import com.mindflow.nova.data.offline.OfflineAttemptRecorder
import com.mindflow.nova.data.remote.currentTzOffsetMinutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

internal const val EMPTY_MISSION_MESSAGE = "Esta misión no tiene contenido compatible con esta versión de NOVA. Vuelve a intentarlo más tarde."

sealed class LessonState {
    /** Se está pidiendo el contenido. */
    object Loading : LessonState()

    data class Error(val message: String) : LessonState()

    /**
     * La lección está lista para jugarse. [attemptId] es el id del intento,
     * creado en el teléfono; [attemptNumber] sube con cada reintento: la
     * pantalla lo usa para volver a montarse desde cero.
     */
    data class Playing(
        val content: MissionContent,
        val attemptId: String,
        val attemptNumber: Int
    ) : LessonState()
}

/**
 * Coordina una lección: pide su contenido, crea el intento en el teléfono y lo
 * entrega al [AttemptRecorder] cuando la pantalla de la mecánica termina. No
 * hace falta conexión para jugar: el intento se califica y se guarda en el
 * teléfono, y se sube cuando se puede. Las pantallas solo dibujan y avisan.
 */
class LessonViewModel(
    private val repository: MissionRepository = RemoteMissionRepository(),
    private val recorder: AttemptRecorder = OfflineAttemptRecorder(
        local = AppServices.localStore,
        sync = AppServices.sync,
        currentUserId = { AppServices.session.currentUserId() },
        scheduleSync = { AppServices.scheduleSync() }
    ),
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
    private val tzOffset: () -> Int = ::currentTzOffsetMinutes,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val snapshotStore: LocalStore? = null,
    private val currentUserId: () -> Int? = { null }
) : ViewModel() {

    private val _state = MutableStateFlow<LessonState>(LessonState.Loading)
    val state: StateFlow<LessonState> = _state.asStateFlow()

    private var missionId: Int? = null
    private var content: MissionContent? = null
    private var attemptNumber = 0
    private var startedAtMs = 0L

    // Reloj del intento en curso: cuánto estuvo en pausa (la app en segundo
    // plano) y si se compró el "+30 s". Se reinicia con cada intento.
    private var pausedAtMs: Long? = null
    private var pausedTotalMs = 0L
    private var usedExtraTime = false
    private var attemptTzOffset = 0
    private var ownerId: Int? = null

    /** Carga la misión y empieza su primer intento. Llamarlo de nuevo con la misma misión no hace nada. */
    fun open(missionId: Int) {
        if (this.missionId == missionId) return
        this.missionId = missionId
        content = null
        attemptNumber = 0
        _state.value = LessonState.Loading

        viewModelScope.launch {
            ownerId = currentUserId()
            val restoredId = savedState.get<String>("attemptId")
            val canRestore = savedState.get<Int>("missionId") == missionId &&
                savedState.get<Int>("userId") == ownerId && restoredId != null
            if (canRestore) {
                val restoredContent = if (snapshotStore != null && ownerId != null) {
                    localOrNull { snapshotStore.activeLesson(ownerId!!, restoredId!!) }
                } else null
                if (restoredContent != null && restoredContent.id == missionId && restoredContent.hasPlayableContent()) {
                    content = restoredContent
                    attemptNumber = savedState["attemptNumber"] ?: 1
                    startedAtMs = savedState["startedAtMs"] ?: now()
                    // Si el proceso murió con el reloj corriendo (en Android 7 y 8
                    // el estado se guarda antes de ON_STOP, así que la pausa pudo no
                    // quedar anotada), lo que pasó desde el último segundo jugado
                    // fue fuera de la app: cuenta como pausa.
                    pausedAtMs = savedState["pausedAtMs"]
                        ?: savedState.get<Long>(LAST_TICK).takeIf { savedState.get<Long>("finishedAtMs") == null }
                    pausedTotalMs = savedState["pausedTotalMs"] ?: 0L
                    usedExtraTime = savedState["usedExtraTime"] ?: false
                    attemptTzOffset = savedState["tzOffset"] ?: tzOffset()
                    _state.value = LessonState.Playing(restoredContent, restoredId!!, attemptNumber)
                    return@launch
                }
                // No reiniciar con respuestas guardadas contra contenido nuevo.
                _state.value = LessonState.Error("No pudimos recuperar esta misión. Vuelve a abrirla para iniciar un intento nuevo.")
                return@launch
            }
            when (val result = repository.loadContent(missionId)) {
                is MissionContentResult.Failed -> _state.value = LessonState.Error(result.message)
                is MissionContentResult.Loaded -> if (!result.content.hasPlayableContent()) {
                    _state.value = LessonState.Error(EMPTY_MISSION_MESSAGE)
                } else {
                    content = result.content
                    startAttempt(result.content)
                }
            }
        }
    }

    /**
     * Empieza un intento nuevo de la misma misión (por ejemplo tras quedarse
     * sin plumas), con otro id y otra hora de inicio, sobre el mismo contenido.
     */
    fun retry() {
        val content = content ?: return
        viewModelScope.launch { startAttempt(content) }
    }

    /** La app pasó a segundo plano: el reloj del intento se detiene. Repetirlo no cuenta doble. */
    fun pauseClock() {
        if (pausedAtMs == null) pausedAtMs = now()
        saveClock()
    }

    /**
     * La app volvió: el reloj sigue. Devuelve cuántos segundos de esta pausa
     * pasaron del tope de pausa gratis ([MAX_PAUSE_SECONDS] sumando todas las
     * pausas del intento): el servidor los cuenta como jugados, así que la
     * pantalla tiene que descontarlos del reloj.
     */
    fun resumeClock(): Int {
        val since = pausedAtMs ?: return 0
        val overCapBefore = secondsOverPauseCap(pausedTotalMs)
        pausedTotalMs += (now() - since).coerceAtLeast(0L)
        pausedAtMs = null
        savedState[LAST_TICK] = now()
        saveClock()
        return secondsOverPauseCap(pausedTotalMs) - overCapBefore
    }

    /**
     * Pasó un segundo con el reloj corriendo. Se anota para que, si Android
     * mata el proceso antes de registrar la pausa, el reloj se recupere desde
     * el último segundo jugado y no desde que empezó el intento.
     */
    fun clockTick() {
        if (pausedAtMs == null) savedState[LAST_TICK] = now()
    }

    private fun secondsOverPauseCap(pausedMs: Long): Int =
        ((pausedMs / 1000).toInt() - MAX_PAUSE_SECONDS).coerceAtLeast(0)

    /** El estudiante compró el "+30 s" en este intento. Se cobra al guardarlo. */
    fun buyExtraTime() {
        usedExtraTime = true
        saveClock()
    }

    /** Guarda el intento en curso y dice cómo quedó: subido, pendiente, rechazado o sin guardar. */
    suspend fun finishAttempt(answers: List<AnswerSubmission>, timedOut: Boolean): LessonOutcome {
        val playing = _state.value as? LessonState.Playing ?: return LessonOutcome.SaveFailed
        if (currentUserId() != ownerId) return LessonOutcome.SaveFailed
        // Un reenvío tras recrear el proceso sigue siendo el mismo intento,
        // con el mismo fin; no consume tiempo mientras se guarda/sincroniza.
        val finishedAtMs = savedState.get<Long>("finishedAtMs") ?: now().also { savedState["finishedAtMs"] = it }
        // Si termina estando en pausa, la pausa cuenta hasta el final.
        val pausedMs = pausedTotalMs + (pausedAtMs?.let { finishedAtMs - it } ?: 0L)

        return recorder.record(
            FinishedAttempt(
                clientAttemptId = playing.attemptId,
                content = playing.content,
                answers = answers,
                timedOut = timedOut,
                startedAtMs = startedAtMs,
                finishedAtMs = finishedAtMs,
                tzOffsetMinutes = attemptTzOffset,
                pausedSeconds = (pausedMs / 1000).toInt(),
                usedExtraTime = usedExtraTime
            )
        )
    }

    private suspend fun startAttempt(content: MissionContent) {
        val id = newId()
        try {
            ownerId?.let { userId ->
                snapshotStore?.clearActiveLessons(userId)
                snapshotStore?.saveActiveLesson(userId, id, content)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = LessonState.Error("No pudimos guardar la misión en este dispositivo. Libera espacio y vuelve a intentarlo.")
            return
        }
        attemptNumber += 1
        startedAtMs = now()
        pausedAtMs = null
        pausedTotalMs = 0L
        usedExtraTime = false
        attemptTzOffset = tzOffset()
        savedState["missionId"] = content.id
        savedState["userId"] = ownerId
        savedState["attemptId"] = id
        savedState["attemptNumber"] = attemptNumber
        savedState["startedAtMs"] = startedAtMs
        savedState.remove<Long>("finishedAtMs")
        // El reloj de relacionar conceptos corre desde el inicio del intento: si
        // Android guarda el estado antes del primer segundo y antes de ON_STOP,
        // la pausa se cuenta desde acá. Las misiones sin reloj no tienen referencia.
        if (content.mechanic == "matching") savedState[LAST_TICK] = startedAtMs else savedState.remove<Long>(LAST_TICK)
        savedState["tzOffset"] = attemptTzOffset
        saveClock()
        _state.value = LessonState.Playing(content, id, attemptNumber)
    }

    private fun saveClock() {
        savedState["pausedAtMs"] = pausedAtMs
        savedState["pausedTotalMs"] = pausedTotalMs
        savedState["usedExtraTime"] = usedExtraTime
    }

    companion object {
        /** Último segundo jugado con el reloj corriendo (epoch ms). */
        private const val LAST_TICK = "lastTickMs"

        val Factory = viewModelFactory {
            initializer {
                LessonViewModel(
                    savedState = createSavedStateHandle(),
                    snapshotStore = AppServices.localStore,
                    currentUserId = { AppServices.session.currentUserId() }
                )
            }
        }
    }
}

package com.mindflow.nova.ui.screens.lessons

import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionOption
import com.mindflow.nova.data.model.MissionPair
import com.mindflow.nova.data.model.MissionQuestion
import com.mindflow.nova.data.offline.AttemptRecorder
import com.mindflow.nova.data.offline.FinishedAttempt
import com.mindflow.nova.data.offline.LessonOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

    private val question = MissionQuestion(
        id = 1, prompt = "¿Verdadero?", type = "true_false", feedback = null, orderIndex = 1, points = 10,
        options = listOf(MissionOption(id = 1, text = "Verdadero", isCorrect = true, feedback = null, orderIndex = 1), MissionOption(id = 2, text = "Falso", isCorrect = false, feedback = null, orderIndex = 2))
    )

    private fun content(id: Int, questions: List<MissionQuestion> = listOf(question)) = MissionContent(
        id = id, levelId = 1, title = "Misión $id", description = null, topic = null, orderIndex = id,
        pointsReward = 100, mechanic = "true_false", timeLimitSeconds = null, maxPlumas = 3, questions = questions
    )

    private val result = AttemptResult(
        id = 0, missionId = 3, score = 100, correctAnswers = 8, wrongAnswers = 0,
        plumasLeft = 3, pointsEarned = 200, isReview = false, status = "completed"
    )

    /** Repositorio falso: responde lo configurado y cuenta cuántas veces se le pidió contenido. */
    private class FakeRepository(var contentResult: (Int) -> MissionContentResult) : MissionRepository {
        var contentCalls = 0

        override suspend fun loadContent(missionId: Int): MissionContentResult {
            contentCalls++
            return contentResult(missionId)
        }

        override suspend fun prefetch(missionIds: List<Int>) = Unit
    }

    private class FakeRecorder(var outcome: LessonOutcome) : AttemptRecorder {
        val recorded = mutableListOf<FinishedAttempt>()

        override suspend fun record(attempt: FinishedAttempt): LessonOutcome {
            recorded += attempt
            return outcome
        }
    }

    private val repo = FakeRepository { MissionContentResult.Loaded(content(it)) }
    private val recorder = FakeRecorder(LessonOutcome.Synced(result))
    private var clock = 1_000_000L
    private var nextId = 0

    private fun viewModel(savedState: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle(), snapshots: com.mindflow.nova.data.local.LocalStore? = null, userId: Int? = null) = LessonViewModel(
        repository = repo,
        recorder = recorder,
        now = { clock },
        newId = { "intento-${++nextId}" },
        tzOffset = { -360 },
        savedState = savedState, snapshotStore = snapshots, currentUserId = { userId }
    )

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `antes de abrir esta cargando y no pidio nada`() {
        val viewModel = viewModel()

        assertEquals(LessonState.Loading, viewModel.state.value)
        assertEquals(0, repo.contentCalls)
    }

    @Test
    fun `abrir carga el contenido y crea el intento en el telefono`() {
        val viewModel = viewModel()

        viewModel.open(3)

        assertEquals(LessonState.Playing(content(3), "intento-1", 1), viewModel.state.value)
    }

    @Test
    fun `abrir la misma mision dos veces no vuelve a pedir nada`() {
        val viewModel = viewModel()

        viewModel.open(3)
        viewModel.open(3)

        assertEquals(1, repo.contentCalls)
        assertEquals(1, nextId)
    }

    @Test
    fun `si el contenido no carga queda el error`() {
        repo.contentResult = { MissionContentResult.Failed("Esta misión todavía no está descargada.") }
        val viewModel = viewModel()

        viewModel.open(3)

        assertEquals(LessonState.Error("Esta misión todavía no está descargada."), viewModel.state.value)
    }

    @Test
    fun `una mision sin preguntas avisa y no crea ningun intento`() {
        repo.contentResult = { MissionContentResult.Loaded(content(it, questions = emptyList())) }
        val viewModel = viewModel()

        viewModel.open(5)

        assertEquals(LessonState.Error(EMPTY_MISSION_MESSAGE), viewModel.state.value)
        assertEquals(0, nextId)
    }

    @Test
    fun `reintentar crea otro intento sin volver a pedir el contenido`() {
        val viewModel = viewModel()
        viewModel.open(3)

        viewModel.retry()

        assertEquals(LessonState.Playing(content(3), "intento-2", 2), viewModel.state.value)
        assertEquals(1, repo.contentCalls)
    }

    @Test
    fun `reintentar sin contenido cargado no hace nada`() {
        repo.contentResult = { MissionContentResult.Failed("x") }
        val viewModel = viewModel()
        viewModel.open(3)

        viewModel.retry()

        assertEquals(LessonState.Error("x"), viewModel.state.value)
    }

    @Test
    fun `terminar entrega al recorder el intento en curso con sus horas`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        clock += 5_000
        viewModel.retry() // el intento en curso ahora es el 2, empezado en este momento
        val startedRetry = clock
        clock += 42_000
        val answers = listOf(AnswerSubmission(questionId = 1, selectedOptionId = 1))

        val outcome = viewModel.finishAttempt(answers, timedOut = true)

        assertEquals(LessonOutcome.Synced(result), outcome)
        assertEquals(
            listOf(FinishedAttempt("intento-2", content(3), answers, true, startedRetry, clock, -360)),
            recorder.recorded
        )
    }

    @Test
    fun `la pausa del reloj se descuenta del intento`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        clock += 10_000
        viewModel.pauseClock()
        clock += 60_000
        viewModel.resumeClock()
        clock += 30_000

        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(60, recorder.recorded.single().pausedSeconds)
        assertFalse(recorder.recorded.single().usedExtraTime)
    }

    @Test
    fun `pausar dos veces seguidas no cuenta doble`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.pauseClock()
        clock += 20_000
        viewModel.pauseClock()
        clock += 20_000
        viewModel.resumeClock()
        viewModel.resumeClock()

        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(40, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `volver de una pausa corta no adelanta el reloj`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.pauseClock()
        clock += 60_000

        assertEquals(0, viewModel.resumeClock())
    }

    @Test
    fun `volver de una pausa de mas de cinco minutos adelanta el reloj lo que se paso`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.pauseClock()
        clock += 400_000

        assertEquals(100, viewModel.resumeClock())
    }

    @Test
    fun `los cinco minutos libres se cuentan sumando todas las pausas`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.pauseClock()
        clock += 200_000
        assertEquals(0, viewModel.resumeClock())
        viewModel.pauseClock()
        clock += 200_000

        assertEquals(100, viewModel.resumeClock())
        assertEquals(0, viewModel.resumeClock())
    }

    @Test
    fun `terminar en pausa cierra la pausa con la hora de fin`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.pauseClock()
        clock += 15_500

        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(15, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `comprar +30 s queda en el intento`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)

        viewModel.buyExtraTime()
        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertTrue(recorder.recorded.single().usedExtraTime)
    }

    @Test
    fun `reintentar empieza sin pausa ni potenciador`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)
        viewModel.buyExtraTime()
        viewModel.pauseClock()
        clock += 30_000
        viewModel.resumeClock()

        viewModel.retry()
        viewModel.finishAttempt(emptyList(), timedOut = false)

        assertEquals(0, recorder.recorded.single().pausedSeconds)
        assertFalse(recorder.recorded.single().usedExtraTime)
    }

    @Test
    fun `terminar devuelve lo que diga el recorder`() = runTest {
        recorder.outcome = LessonOutcome.Rejected("Primero completa la misión anterior")
        val viewModel = viewModel()
        viewModel.open(3)

        assertEquals(LessonOutcome.Rejected("Primero completa la misión anterior"), viewModel.finishAttempt(emptyList(), false))
    }

    @Test
    fun `terminar sin una leccion en curso no guarda nada`() = runTest {
        val viewModel = viewModel()

        assertEquals(LessonOutcome.SaveFailed, viewModel.finishAttempt(emptyList(), timedOut = false))
        assertTrue(recorder.recorded.isEmpty())
    }

    @Test
    fun `recrear conserva id contenido original reloj y potenciador`() = runTest {
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        val first = viewModel(handle, local, 7)
        first.open(3)
        val playing = first.state.value as LessonState.Playing
        first.buyExtraTime()
        clock += 10_000
        first.pauseClock()
        clock += 30_000
        val restoredHandle = androidx.lifecycle.SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })
        repo.contentResult = { MissionContentResult.Failed("sin red") }
        val restored = viewModel(restoredHandle, local, 7)
        restored.open(3)
        assertEquals(playing, restored.state.value)
        assertEquals(1, repo.contentCalls)
        restored.resumeClock()
        restored.finishAttempt(emptyList(), false)
        val finished = recorder.recorded.single()
        assertEquals(playing.attemptId, finished.clientAttemptId)
        assertEquals(1_000_000L, finished.startedAtMs)
        assertEquals(30, finished.pausedSeconds)
        assertTrue(finished.usedExtraTime)
    }

    /** Lo que Android guarda del ViewModel, como si el proceso muriera y se recreara. */
    private fun afterProcessDeath(handle: androidx.lifecycle.SavedStateHandle, local: com.mindflow.nova.data.local.LocalStore): LessonViewModel {
        val restoredHandle = androidx.lifecycle.SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })
        repo.contentResult = { MissionContentResult.Failed("sin red") }
        return viewModel(restoredHandle, local, 7).also { it.open(3) }
    }

    @Test
    fun `si el proceso muere con el reloj corriendo lo de afuera cuenta como pausa`() = runTest {
        // Android 7 y 8: el estado se guarda antes de ON_STOP, sin la pausa anotada.
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        viewModel(handle, local, 7).apply {
            open(3)
            clock += 20_000
            clockTick()
        }
        clock += 60_000

        val restored = afterProcessDeath(handle, local)

        assertEquals(0, restored.resumeClock())
        restored.finishAttempt(emptyList(), false)
        assertEquals(60, recorder.recorded.single().pausedSeconds)
    }

    private fun matchingContent(id: Int) = content(
        id,
        listOf(
            MissionQuestion(
                id = 9, prompt = "Relaciona", type = "matching", feedback = null, orderIndex = 1, points = 10,
                pairs = listOf(MissionPair(id = 1, term = "Igualdad", match = "Mismos derechos", orderIndex = 1))
            )
        )
    ).copy(mechanic = "matching", timeLimitSeconds = 45)

    /** Lo que Android guardó en ese momento, para recuperarlo tras matar el proceso. */
    private fun snapshot(handle: androidx.lifecycle.SavedStateHandle) =
        androidx.lifecycle.SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) })

    @Test
    fun `matching guardado antes del primer segundo y de ON_STOP cuenta la pausa desde el inicio`() = runTest {
        // Android 7 y 8: el estado se guarda antes de ON_STOP, y acá también antes del primer tick.
        repo.contentResult = { MissionContentResult.Loaded(matchingContent(it)) }
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        val first = viewModel(handle, local, 7)
        first.open(3)
        val saved = snapshot(handle)
        // ON_STOP llega después de guardar: esta pausa no está en lo guardado.
        first.pauseClock()
        clock += 60_000

        val restored = viewModel(saved, local, 7)
        restored.open(3)

        assertEquals(0, restored.resumeClock())
        restored.finishAttempt(emptyList(), false)
        assertEquals(60, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `sin reloj no se inventa una pausa al recuperar el proceso`() = runTest {
        // Verdadero o falso no tiene reloj: no hay ticks ni referencia que convertir en pausa.
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        viewModel(handle, local, 7).open(3)
        clock += 60_000

        val restored = afterProcessDeath(handle, local)

        restored.finishAttempt(emptyList(), false)
        assertEquals(0, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `una pausa ya anotada antes de morir el proceso no se cuenta dos veces`() = runTest {
        // Android 9 en adelante: ON_STOP llega antes de guardar el estado.
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        viewModel(handle, local, 7).apply {
            open(3)
            clock += 20_000
            clockTick()
            clock += 500
            pauseClock()
        }
        clock += 60_000

        val restored = afterProcessDeath(handle, local)

        restored.resumeClock()
        restored.finishAttempt(emptyList(), false)
        assertEquals(60, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `volver de una pausa no reusa el ultimo segundo de antes de pausar`() = runTest {
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        viewModel(handle, local, 7).apply {
            open(3)
            clockTick()
            pauseClock()
            clock += 40_000
            resumeClock()
        }
        // Muere sin otro segundo jugado: solo cuenta lo que pasó desde que volvió.
        clock += 10_000

        val restored = afterProcessDeath(handle, local)

        restored.resumeClock()
        restored.finishAttempt(emptyList(), false)
        assertEquals(50, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `el tope de pausa y el +30 s siguen al recrear el proceso`() = runTest {
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        viewModel(handle, local, 7).apply {
            open(3)
            buyExtraTime()
            pauseClock()
            clock += 200_000
            resumeClock()
            clockTick()
            pauseClock()
        }
        clock += 150_000

        val restored = afterProcessDeath(handle, local)

        // 200 s + 150 s de pausa: los 300 s gratis se agotaron y 50 s corren como jugados.
        assertEquals(50, restored.resumeClock())
        restored.finishAttempt(emptyList(), false)
        val finished = recorder.recorded.single()
        assertEquals(350, finished.pausedSeconds)
        assertTrue(finished.usedExtraTime)
    }

    @Test
    fun `reanudar sin pausa no descuenta nada`() = runTest {
        val viewModel = viewModel()
        viewModel.open(3)

        assertEquals(0, viewModel.resumeClock())
        viewModel.finishAttempt(emptyList(), false)
        assertEquals(0, recorder.recorded.single().pausedSeconds)
    }

    @Test
    fun `otra cuenta no restaura un intento ajeno`() = runTest {
        val handle = androidx.lifecycle.SavedStateHandle()
        val local = com.mindflow.nova.data.local.FakeLocalStore()
        val first = viewModel(handle, local, 7)
        first.open(3)
        val second = viewModel(handle, local, 8)
        second.open(3)
        assertEquals(2, nextId)
        assertFalse((first.state.value as LessonState.Playing).attemptId == (second.state.value as LessonState.Playing).attemptId)
    }
}

package com.mindflow.nova.data.mission

import com.mindflow.nova.AppServices
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.local.localOrNull
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.connectionErrorMessage
import com.mindflow.nova.data.remote.httpErrorMessage
import kotlinx.coroutines.CancellationException

/** Resultado de pedir el contenido jugable de una misión. */
sealed class MissionContentResult {
    data class Loaded(val content: MissionContent) : MissionContentResult()
    data class Failed(val message: String) : MissionContentResult()
}

/**
 * Contenido de las misiones. Las pantallas de lección no llaman a la API; se
 * lo piden al ViewModel, y el ViewModel a este repositorio.
 */
interface MissionRepository {
    suspend fun loadContent(missionId: Int): MissionContentResult

    /**
     * Baja y guarda las misiones que todavía no estén en el teléfono o que se
     * guardaron hace más de 24 h, para poder jugarlas sin conexión.
     */
    suspend fun prefetch(missionIds: List<Int>)
}

/** El contenido se pide al servidor y se guarda; sin respuesta, se juega la copia guardada. */
class RemoteMissionRepository(
    private val api: () -> NovaApiService = { RetrofitClient.api },
    private val local: LocalStore = AppServices.localStore,
    private val now: () -> Long = System::currentTimeMillis,
    private val currentUserId: () -> Int? = { AppServices.session.currentUserId() },
    private val tokenFor: (Int) -> String? = { AppServices.session.tokenFor(it) }
) : MissionRepository {

    override suspend fun loadContent(missionId: Int): MissionContentResult {
        val userId = currentUserId() ?: return MissionContentResult.Failed(SESSION_CHANGED_MESSAGE)
        val token = tokenFor(userId) ?: return MissionContentResult.Failed(SESSION_CHANGED_MESSAGE)
        val failure = try {
            val response = api().getMissionContent(missionId, "Bearer $token")
            if (currentUserId() != userId || tokenFor(userId) != token) return MissionContentResult.Failed(SESSION_CHANGED_MESSAGE)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                localOrNull { local.saveMission(userId, body.mission) }
                return MissionContentResult.Loaded(body.mission)
            }

            val message = httpErrorMessage(response.code(), response.errorBody()?.string(), "No se pudo cargar la misión")
            // La misión ya no existe o no está publicada: la copia guardada no sirve.
            if (response.code() in listOf(401, 403, 404)) return MissionContentResult.Failed(message)
            message
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NOT_DOWNLOADED_MESSAGE
        }

        if (currentUserId() != userId || tokenFor(userId) != token) return MissionContentResult.Failed(SESSION_CHANGED_MESSAGE)
        val cached = localOrNull { local.mission(userId, missionId) }

        return if (cached != null) MissionContentResult.Loaded(cached) else MissionContentResult.Failed(failure)
    }

    override suspend fun prefetch(missionIds: List<Int>) {
        val userId = currentUserId() ?: return
        val token = tokenFor(userId) ?: return
        for (missionId in missionIds.distinct()) {
            if (currentUserId() != userId || tokenFor(userId) != token) return
            val savedAt = localOrNull { local.missionSavedAt(userId, missionId) }
            if (savedAt != null && now() - savedAt < MAX_AGE_MS) continue

            // Una que falle no corta las demás; se vuelve a intentar en la próxima carga.
            try {
                val response = api().getMissionContent(missionId, "Bearer $token")
                val body = response.body()
                if (currentUserId() != userId || tokenFor(userId) != token) return
                if (response.code() == 401 || response.code() == 403) return
                if (response.isSuccessful && body != null) localOrNull { local.saveMission(userId, body.mission) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                continue
            }
        }
    }

    private companion object {
        const val MAX_AGE_MS = 24L * 60 * 60 * 1000
        const val NOT_DOWNLOADED_MESSAGE = "Esta misión todavía no está descargada. Conéctate a internet para bajarla."
        const val SESSION_CHANGED_MESSAGE = "Tu sesión cambió. Vuelve a abrir la misión."
    }
}

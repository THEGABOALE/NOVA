package com.mindflow.nova.data.teacher

import com.mindflow.nova.AppServices
import com.mindflow.nova.data.model.TeacherRoom
import com.mindflow.nova.data.remote.NovaApiService
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.connectionErrorMessage
import com.mindflow.nova.data.remote.httpErrorMessage
import kotlinx.coroutines.CancellationException

sealed class TeacherRoomsResult {
    data class Loaded(val rooms: List<TeacherRoom>) : TeacherRoomsResult()
    data class Failed(val message: String) : TeacherRoomsResult()
}

fun interface TeacherRepository { suspend fun loadRooms(): TeacherRoomsResult }

/** No se cachean rosters docentes en tablets compartidas. */
class RemoteTeacherRepository(
    private val api: () -> NovaApiService = { RetrofitClient.api },
    private val currentUserId: () -> Int? = { AppServices.session.currentUserId() },
    private val tokenFor: (Int) -> String? = { AppServices.session.tokenFor(it) }
) : TeacherRepository {
    override suspend fun loadRooms(): TeacherRoomsResult {
        val userId = currentUserId() ?: return TeacherRoomsResult.Failed("Vuelve a iniciar sesión.")
        val token = tokenFor(userId) ?: return TeacherRoomsResult.Failed("Vuelve a iniciar sesión.")
        return try {
            val response = api().getTeacherRooms("Bearer $token")
            val body = response.body()
            when {
                currentUserId() != userId || tokenFor(userId) != token -> TeacherRoomsResult.Failed("Tu sesión cambió. Vuelve a iniciar sesión.")
                response.isSuccessful && body != null -> TeacherRoomsResult.Loaded(body.rooms)
                else -> TeacherRoomsResult.Failed(httpErrorMessage(response.code(), response.errorBody()?.string(), "No pudimos cargar tus salas."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            TeacherRoomsResult.Failed(connectionErrorMessage(e))
        }
    }
}

package com.mindflow.nova.data.teacher

import com.google.gson.Gson
import com.mindflow.nova.data.model.TeacherRoomsResponse
import com.mindflow.nova.data.remote.FakeNovaApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteTeacherRepositoryTest {
    private val api = FakeNovaApi()
    private var user = 7
    private val repository = RemoteTeacherRepository(api = { api }, currentUserId = { user }, tokenFor = { "token-$user" })

    @Test fun backendContractLoadsRealRoomAndNullAttempt() = runTest {
        val body = Gson().fromJson("""{"status":"OK","rooms":[{"id":1,"name":"Sala A","grade":"Quinto","section":"A","schoolYear":2026,"levelId":2,"studentCount":1,"students":[{"id":11,"fullName":"Estudiante de prueba","totalPoints":30,"missionsCompleted":2,"lastAttemptAt":null,"lastAttemptSeconds":null}],"resultsByMechanic":[{"mechanic":"matching","averageScore":82.5}]}]}""", TeacherRoomsResponse::class.java)
        api.onTeacherRooms = { Response.success(body) }
        val result = repository.loadRooms() as TeacherRoomsResult.Loaded
        assertEquals("Sala A", result.rooms.single().name)
        assertNull(result.rooms.single().students.single().lastAttemptSeconds)
        assertEquals(82.5, result.rooms.single().resultsByMechanic.single().averageScore, 0.0)
    }

    @Test fun noRoomsIsARealEmptyState() = runTest {
        api.onTeacherRooms = { Response.success(TeacherRoomsResponse(null, "OK", emptyList())) }
        assertEquals(TeacherRoomsResult.Loaded(emptyList()), repository.loadRooms())
    }

    @Test fun authorizationFailureNeverBecomesFakeData() = runTest {
        for (status in listOf(401, 403, 500)) {
            api.onTeacherRooms = { FakeNovaApi.httpError(status) }
            assertTrue(repository.loadRooms() is TeacherRoomsResult.Failed)
        }
    }

    @Test fun lateResponseFromPreviousAccountIsDiscarded() = runTest {
        api.onTeacherRooms = { user = 8; Response.success(TeacherRoomsResponse(null, "OK", emptyList())) }
        assertTrue(repository.loadRooms() is TeacherRoomsResult.Failed)
    }

    @Test(expected = CancellationException::class) fun cancellationIsPropagated() = runTest {
        api.onTeacherRooms = { throw CancellationException("cancelado") }
        repository.loadRooms()
    }
}

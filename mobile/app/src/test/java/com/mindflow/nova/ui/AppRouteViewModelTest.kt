package com.mindflow.nova.ui

import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.session.SessionResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppRouteViewModelTest {

    private val student = SessionUser(
        id = 7, fullName = "Ana", email = null, loginId = "ana7", role = "student", centerId = 1, group = null
    )

    private var calls = 0
    private var result: SessionResult = SessionResult.Success(student)
    private var gate: CompletableDeferred<Unit>? = null

    private fun viewModel() = AppRouteViewModel {
        calls++
        gate?.await()
        result
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `valida la sesion una sola vez al crearse`() {
        val route = viewModel()

        assertEquals(1, calls)
        assertEquals(AppScreen.Onboarding, route.screen.value)
    }

    @Test
    fun `mientras valida muestra cargando`() {
        gate = CompletableDeferred()
        val route = viewModel()

        assertEquals(AppScreen.Loading, route.screen.value)
        gate!!.complete(Unit)
        assertEquals(AppScreen.Onboarding, route.screen.value)
    }

    @Test
    fun `una sesion rechazada manda al login`() {
        result = SessionResult.Rejected("La sesión ya no es válida")

        assertEquals(AppScreen.Login, viewModel().screen.value)
    }

    @Test
    fun `sin poder validar muestra el error de conexion y reintentar vuelve a validar`() {
        result = SessionResult.Failure("Sin conexión")
        val route = viewModel()
        assertEquals(AppScreen.ConnectionError, route.screen.value)

        result = SessionResult.Success(student.copy(role = "teacher"))
        route.validate()

        assertEquals(2, calls)
        assertEquals(AppScreen.TeacherHome, route.screen.value)
    }

    @Test
    fun `cambiar de pantalla no vuelve a validar`() {
        val route = viewModel()

        route.show(AppScreen.StudentHome)
        route.show(AppScreen.Login)

        assertEquals(1, calls)
        assertEquals(AppScreen.Login, route.screen.value)
    }
}

package com.mindflow.nova.ui.screens.teacher

import com.mindflow.nova.data.teacher.TeacherRepository
import com.mindflow.nova.data.teacher.TeacherRoomsResult
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
class TeacherRoomsViewModelTest {

    private var calls = 0
    private var result: TeacherRoomsResult = TeacherRoomsResult.Loaded(emptyList())
    private val repository = TeacherRepository { calls++; result }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `carga las salas una sola vez al crearse`() {
        val viewModel = TeacherRoomsViewModel(repository)

        assertEquals(1, calls)
        assertEquals(TeacherRoomsState(loading = false), viewModel.state.value)
    }

    @Test
    fun `un error queda visible y reintentar vuelve a pedir las salas`() {
        result = TeacherRoomsResult.Failed("No pudimos cargar tus salas.")
        val viewModel = TeacherRoomsViewModel(repository)
        assertEquals("No pudimos cargar tus salas.", viewModel.state.value.error)

        result = TeacherRoomsResult.Loaded(emptyList())
        viewModel.refresh()

        assertEquals(2, calls)
        assertEquals(TeacherRoomsState(loading = false), viewModel.state.value)
    }
}

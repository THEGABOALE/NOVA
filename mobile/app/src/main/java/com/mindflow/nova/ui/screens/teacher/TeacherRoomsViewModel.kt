package com.mindflow.nova.ui.screens.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindflow.nova.data.model.TeacherRoom
import com.mindflow.nova.data.teacher.RemoteTeacherRepository
import com.mindflow.nova.data.teacher.TeacherRepository
import com.mindflow.nova.data.teacher.TeacherRoomsResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherRoomsState(val loading: Boolean = true, val rooms: List<TeacherRoom> = emptyList(), val error: String? = null)

/**
 * Carga las salas al crearse. Como el ViewModel sobrevive a girar el teléfono,
 * la rotación no vuelve a pedir el roster; "Actualizar" y "Reintentar" sí.
 */
class TeacherRoomsViewModel(private val repository: TeacherRepository = RemoteTeacherRepository()) : ViewModel() {
    private val mutableState = MutableStateFlow(TeacherRoomsState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        job?.cancel()
        job = viewModelScope.launch {
            mutableState.value = TeacherRoomsState()
            mutableState.value = when (val result = repository.loadRooms()) {
                is TeacherRoomsResult.Loaded -> TeacherRoomsState(loading = false, rooms = result.rooms)
                is TeacherRoomsResult.Failed -> TeacherRoomsState(loading = false, error = result.message)
            }
        }
    }
}

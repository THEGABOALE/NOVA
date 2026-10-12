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

class TeacherRoomsViewModel(private val repository: TeacherRepository = RemoteTeacherRepository()) : ViewModel() {
    private val mutableState = MutableStateFlow(TeacherRoomsState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

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

package com.mindflow.nova.ui.screens.teacher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindflow.nova.R
import com.mindflow.nova.ui.screens.lessons.mechanicLabel
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaText

@Composable
fun TeacherRoomsScreen(onLogout: () -> Unit, viewModel: TeacherRoomsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    TeacherRoomsContent(state, viewModel::refresh, onLogout)
}

@Composable
internal fun TeacherRoomsContent(state: TeacherRoomsState, onRefresh: () -> Unit, onLogout: () -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val room = state.rooms.firstOrNull { it.id == selectedId }
    BackHandler(enabled = selectedId != null) { selectedId = null }

    Scaffold(containerColor = NovaBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(room?.name ?: stringResource(R.string.teacher_rooms), style = MaterialTheme.typography.headlineMedium, color = NovaText)
                if (selectedId != null) TextButton(onClick = { selectedId = null }) { Text(stringResource(R.string.teacher_back)) }
                OutlinedButton(onClick = onRefresh, enabled = !state.loading) { Text(stringResource(R.string.teacher_refresh)) }
                TextButton(onClick = { confirmLogout = true }) { Text(stringResource(R.string.sign_out)) }
            }
            when {
                state.loading -> item { CircularProgressIndicator() }
                state.error != null -> item {
                    Text(state.error, style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = onRefresh) { Text(stringResource(R.string.retry)) }
                }
                room != null -> {
                    item {
                        Text(stringResource(R.string.teacher_room_info, room.grade, room.section.orEmpty(), room.schoolYear), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.teacher_student_count, room.studentCount), style = MaterialTheme.typography.titleMedium)
                    }
                    item { Text(stringResource(R.string.teacher_summary), style = MaterialTheme.typography.titleLarge) }
                    if (room.resultsByMechanic.isEmpty()) item { Text(stringResource(R.string.teacher_no_results)) }
                    items(room.resultsByMechanic, key = { it.mechanic }) { result ->
                        Text(stringResource(R.string.teacher_mechanic_result, mechanicLabel(result.mechanic), result.averageScore), style = MaterialTheme.typography.bodyLarge)
                    }
                    item { Text(stringResource(R.string.teacher_students), style = MaterialTheme.typography.titleLarge) }
                    if (room.students.isEmpty()) item { Text(stringResource(R.string.teacher_no_students)) }
                    items(room.students, key = { it.id }) { student ->
                        Surface(shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(student.fullName, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.teacher_student_progress, student.missionsCompleted, student.totalPoints))
                                student.lastAttemptSeconds?.let { Text(stringResource(R.string.teacher_last_time, it)) }
                            }
                        }
                    }
                }
                state.rooms.isEmpty() -> item { Text(stringResource(R.string.teacher_no_rooms)) }
                else -> items(state.rooms, key = { it.id }) { available ->
                    OutlinedButton(
                        onClick = { selectedId = available.id },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) { Text(available.name, style = MaterialTheme.typography.titleMedium) }
                }
            }
        }
    }
    if (confirmLogout) AlertDialog(
        onDismissRequest = { confirmLogout = false },
        title = { Text(stringResource(R.string.sign_out_question)) },
        text = { Text(stringResource(R.string.sign_out_description)) },
        confirmButton = { TextButton(onClick = onLogout) { Text(stringResource(R.string.sign_out)) } },
        dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.cancel)) } }
    )
}

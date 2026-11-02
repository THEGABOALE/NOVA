package com.mindflow.nova.ui.screens.teacher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindflow.nova.R
import com.mindflow.nova.data.model.TeacherMechanicResult
import com.mindflow.nova.data.model.TeacherRoom
import com.mindflow.nova.data.model.TeacherStudent
import com.mindflow.nova.ui.screens.lessons.mechanicLabel
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaInfoBackground
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaOnPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaSurface
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import kotlin.math.roundToInt

/**
 * Panel docente: lista de Salas y, dentro de cada una, sus estudiantes y una
 * pestaña de Resumen con el promedio de aciertos por minijuego. Los datos son
 * los de GET /api/teacher/me/students y no se guardan en el teléfono.
 */
@Composable
fun TeacherRoomsScreen(onLogout: () -> Unit, viewModel: TeacherRoomsViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    TeacherRoomsContent(state, viewModel::refresh, onLogout)
}

@Composable
internal fun TeacherRoomsContent(state: TeacherRoomsState, onRefresh: () -> Unit, onLogout: () -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedIndex = state.rooms.indexOfFirst { it.id == selectedId }

    if (selectedIndex >= 0) {
        // "Atrás" del teléfono en el detalle vuelve a la lista de salas.
        BackHandler { selectedId = null }

        TeacherRoomDetailScreen(
            room = state.rooms[selectedIndex],
            roomsAssigned = state.rooms.size,
            onBack = { selectedId = null }
        )
    } else {
        SalasListScreen(
            state = state,
            onRefresh = onRefresh,
            onLogout = onLogout,
            onRoomSelected = { selectedId = it.id }
        )
    }
}

@Composable
private fun SalasListScreen(
    state: TeacherRoomsState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onRoomSelected: (TeacherRoom) -> Unit
) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    Scaffold(containerColor = NovaBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // Cerrar sesión es un botón propio y pide confirmación.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.teacher_rooms),
                    color = NovaText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )

                IconButton(onClick = onRefresh, enabled = !state.loading) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.teacher_refresh),
                        tint = NovaText
                    )
                }

                IconButton(onClick = { confirmLogout = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = stringResource(R.string.sign_out),
                        tint = NovaText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                state.loading -> Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NovaPurple)
                }

                state.error != null -> TeacherMessage(
                    text = state.error,
                    action = stringResource(R.string.retry),
                    onAction = onRefresh
                )

                state.rooms.isEmpty() -> TeacherMessage(text = stringResource(R.string.teacher_no_rooms))

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(state.rooms, key = { _, room -> room.id }) { index, room ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button) { onRoomSelected(room) },
                            shape = RoundedCornerShape(16.dp),
                            color = RoomAccents[index % RoomAccents.size]
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                                // El pastel de la sala es claro también en modo oscuro:
                                // el texto va siempre oscuro para que se lea.
                                Text(
                                    text = room.name,
                                    color = RoomCardText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = roomDetails(room),
                                    color = RoomCardText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(R.string.sign_out_question)) },
            text = { Text(stringResource(R.string.sign_out_description)) },
            confirmButton = {
                TextButton(onClick = onLogout) { Text(stringResource(R.string.sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun TeacherRoomDetailScreen(
    room: TeacherRoom,
    roomsAssigned: Int,
    onBack: () -> Unit
) {
    var selectedTab by rememberSaveable(room.id) { mutableStateOf(0) }
    val tabLabels = listOf(stringResource(R.string.teacher_students), stringResource(R.string.teacher_summary_tab))

    Scaffold(containerColor = NovaBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.teacher_back),
                    tint = NovaText
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = NovaLightPurple
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Groups,
                            contentDescription = null,
                            tint = NovaPurple,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = room.name,
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(value = room.studentCount, label = stringResource(R.string.teacher_stat_students))
                    ProfileStat(value = roomsAssigned, label = stringResource(R.string.teacher_stat_rooms))
                    ProfileStat(value = room.resultsByMechanic.size, label = stringResource(R.string.teacher_stat_minigames))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    color = NovaSurface,
                    border = BorderStroke(1.dp, NovaBorder)
                ) {
                    Text(
                        text = roomDetails(room),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        color = NovaText,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(tabLabels) { index, label ->
                    val isSelected = index == selectedTab
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .semantics { selected = isSelected }
                            .clickable(role = Role.Tab) { selectedTab = index },
                        shape = RoundedCornerShape(50.dp),
                        color = if (isSelected) NovaPurple else NovaSurface,
                        border = if (isSelected) null else BorderStroke(1.dp, NovaBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (isSelected) NovaOnPurple else NovaTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 1) {
                ResumenList(results = room.resultsByMechanic)
            } else {
                RosterList(students = room.students)
            }
        }
    }
}

@Composable
private fun ProfileStat(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), color = NovaText, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(
            text = label,
            color = NovaTextSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
private fun RosterList(students: List<TeacherStudent>) {
    if (students.isEmpty()) {
        TeacherMessage(text = stringResource(R.string.teacher_no_students))
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(students, key = { it.id }) { student ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = NovaLightPurple
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = NovaPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        color = NovaText,
                        fontSize = 13.sp
                    )
                    Text(
                        text = stringResource(R.string.teacher_student_progress, student.missionsCompleted, student.totalPoints),
                        color = NovaTextSecondary,
                        fontSize = 11.sp
                    )
                }

                student.lastAttemptSeconds?.let { seconds ->
                    val duration = formatDuration(seconds)
                    // TalkBack lee "Último intento: 2:30" en vez de solo los números.
                    val description = stringResource(R.string.teacher_last_time, duration)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = duration,
                        color = NovaTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.semantics { contentDescription = description }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenList(results: List<TeacherMechanicResult>) {
    Column {
        // Los resultados vienen de práctica sin conexión: el servidor recalifica
        // las respuestas, pero no puede saber si el alumno vio la solución.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = NovaInfoBackground
        ) {
            Text(
                text = stringResource(R.string.teacher_formative_note),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = NovaText,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (results.isEmpty()) {
            TeacherMessage(text = stringResource(R.string.teacher_no_results))
            return
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.teacher_summary_minigame),
                modifier = Modifier.weight(1f),
                color = NovaTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = stringResource(R.string.teacher_summary_average),
                color = NovaTextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            results.forEach { result ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = NovaLightPurple
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mechanicLabel(result.mechanic),
                            modifier = Modifier.weight(1f),
                            color = NovaPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${result.averageScore.roundToInt()}%",
                            color = NovaPurple,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/** Mensaje de lista vacía o de error, con una acción opcional. */
@Composable
private fun TeacherMessage(text: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = NovaTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        if (action != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = NovaPurple, contentColor = NovaOnPurple)
            ) {
                Text(action, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** "5º grado · Sección A · 2026", sin separadores de más si falta la sección. */
@Composable
private fun roomDetails(room: TeacherRoom): String =
    listOfNotNull(
        room.grade,
        room.section?.takeIf { it.isNotBlank() }?.let { stringResource(R.string.teacher_section, it) },
        room.schoolYear.toString()
    ).joinToString(" · ")

/** Segundos del último intento como "m:ss". */
internal fun formatDuration(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/** Pasteles de las tarjetas de salas; son claros en los dos temas. */
private val RoomAccents = listOf(Color(0xFFCFE0FF), Color(0xFFBFEAE0), Color(0xFFC7CDFB))

/** Texto sobre el pastel de cada sala, que es claro en los dos temas. */
private val RoomCardText = Color(0xFF1E1A22)

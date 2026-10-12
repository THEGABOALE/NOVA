package com.mindflow.nova.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import com.mindflow.nova.data.model.*
import com.mindflow.nova.ui.screens.teacher.*
import com.mindflow.nova.ui.theme.NOVATheme
import org.junit.Rule
import org.junit.Test

class TeacherRoomsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyRoomsShowsRealEmptyState() {
        compose.setContent { NOVATheme(darkTheme = false) { TeacherRoomsContent(TeacherRoomsState(loading = false), {}, {}) } }
        compose.onNodeWithText("Aún no tienes salas asignadas.").assertIsDisplayed()
    }

    @Test fun roomOpensRealStudentsAndSummary() {
        val room = TeacherRoom(1, "Sala de prueba", "Quinto", "A", 2026, 1, 1,
            listOf(TeacherStudent(4, "Nombre desde API", 50, 2, null, null)), emptyList())
        compose.setContent { NOVATheme(darkTheme = false) { TeacherRoomsContent(TeacherRoomsState(loading = false, rooms = listOf(room)), {}, {}) } }
        compose.onNodeWithText("Sala de prueba").performClick()
        compose.onNodeWithText("Nombre desde API").assertIsDisplayed()
        compose.onNodeWithText("Aún no hay resultados en esta sala.").assertIsDisplayed()
    }
}

package com.mindflow.nova.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mindflow.nova.data.model.TeacherMechanicResult
import com.mindflow.nova.data.model.TeacherRoom
import com.mindflow.nova.data.model.TeacherStudent
import com.mindflow.nova.ui.screens.teacher.TeacherRoomsContent
import com.mindflow.nova.ui.screens.teacher.TeacherRoomsState
import com.mindflow.nova.ui.theme.NOVATheme
import org.junit.Rule
import org.junit.Test

class TeacherRoomsScreenTest {
    @get:Rule val compose = createComposeRule()

    private val room = TeacherRoom(
        1, "Sala de prueba", "Quinto grado", null, 2026, 1, 1,
        listOf(TeacherStudent(4, "Nombre desde API", 50, 2, null, 150)),
        listOf(TeacherMechanicResult("true_false", 84.0))
    )

    @Test fun emptyRoomsShowsRealEmptyState() {
        compose.setContent { NOVATheme(darkTheme = false) { TeacherRoomsContent(TeacherRoomsState(loading = false), {}, {}) } }
        compose.onNodeWithText("Aún no tienes salas asignadas.").assertIsDisplayed()
    }

    @Test fun errorOffersRetry() {
        compose.setContent { NOVATheme(darkTheme = true) { TeacherRoomsContent(TeacherRoomsState(loading = false, error = "Sin conexión"), {}, {}) } }
        compose.onNodeWithText("Sin conexión").assertIsDisplayed()
        compose.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test fun roomShowsRealStudentsAndFormativeSummary() {
        compose.setContent { NOVATheme(darkTheme = false) { TeacherRoomsContent(TeacherRoomsState(loading = false, rooms = listOf(room)), {}, {}) } }
        // Sin sección no quedan separadores sueltos.
        compose.onNodeWithText("Quinto grado · 2026").assertIsDisplayed()
        compose.onNodeWithText("Sala de prueba").performClick()

        compose.onNodeWithText("Nombre desde API").assertIsDisplayed()
        compose.onNodeWithContentDescription("Último intento: 2:30").assertIsDisplayed()

        compose.onNodeWithText("Resumen").performClick()
        compose.onNodeWithText("Verdadero o falso").assertIsDisplayed()
        compose.onNodeWithText("84%").assertIsDisplayed()
        compose.onNodeWithText("práctica formativa", substring = true).assertIsDisplayed()
    }
}

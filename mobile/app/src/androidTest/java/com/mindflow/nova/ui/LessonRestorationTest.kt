package com.mindflow.nova.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.unit.Density
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.offline.LessonOutcome
import com.mindflow.nova.ui.screens.lessons.*
import com.mindflow.nova.ui.theme.NOVATheme
import org.junit.Rule
import org.junit.Test

class LessonRestorationTest {
    @get:Rule val compose = createComposeRule()
    private val mission = MissionResponse(1, "Prueba", null, null, 1, 10, "multiple_choice", null, 3, true)
    private val attempt = LessonAttempt("same-attempt", {}, { _, _ ->
        LessonOutcome.Pending(AttemptResult(0, 1, 100, 2, 0, 3, 10, false, "completed"))
    })

    @Test fun multipleChoiceKeepsAnswerFeedbackAndQuestionAfterRecreation() {
        val restoration = StateRestorationTester(compose)
        val questions = listOf(
            LessonQuestion(1, "Primera pregunta", listOf(LessonOption(10, "Respuesta correcta", true, "Muy bien"), LessonOption(11, "Otra opción", false, "No"))),
            LessonQuestion(2, "Segunda pregunta", listOf(LessonOption(20, "Segunda correcta", true, "Sí"), LessonOption(21, "Segunda incorrecta", false, "No")))
        )
        restoration.setContent {
            NOVATheme(darkTheme = false) { LessonPlayScreen(mission, questions, attempt, {}) }
        }
        compose.onNodeWithText("Respuesta correcta").performClick()
        compose.onNodeWithText("Continuar").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("Segunda pregunta").assertIsDisplayed()
    }

    @Test fun trueFalseKeepsSelectedAnswerAndRemainsUsableAtLargeFont() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                NOVATheme(darkTheme = true) { TrueFalseLessonScreen(mission.copy(mechanic = "true_false"), listOf(TrueFalseQuestion(1, "Todos merecen respeto", true, "Respeto", 10, 11)), attempt, {}) }
            }
        }
        compose.onNodeWithText("VERDADERO").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("Ver resultados").assertIsDisplayed()
    }

    @Test fun matchingKeepsMatchedPairsInsteadOfAwardingThemAgain() {
        val restoration = StateRestorationTester(compose)
        compose.mainClock.autoAdvance = false
        restoration.setContent {
            NOVATheme(darkTheme = false) { MatchingLessonScreen(mission.copy(mechanic = "matching", timeLimitSeconds = 60), listOf(MatchingPair(1, "Igualdad", "Mismos derechos"), MatchingPair(2, "Respeto", "Escuchar")), 1, attempt, {}) }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Igualdad").performClick()
        compose.onNodeWithText("Mismos derechos").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Respeto").performClick()
        compose.onNodeWithText("Escuchar").performClick()
        compose.mainClock.advanceTimeBy(100)
        compose.onNodeWithText("Continuar").assertIsDisplayed()
    }
}

package com.mindflow.nova.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.offline.LessonOutcome
import com.mindflow.nova.ui.screens.lessons.LessonAttempt
import com.mindflow.nova.ui.screens.lessons.MatchingLessonScreen
import com.mindflow.nova.ui.screens.lessons.MatchingPair
import com.mindflow.nova.ui.theme.NOVATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * El reloj de la misión de relacionar conceptos se pausa solo cuando la misión
 * deja de verse (ON_STOP) y sigue al volver (ON_START), sin perder el tiempo
 * restante ni el "+30 s" al recrearse la pantalla.
 */
class MatchingClockLifecycleTest {
    @get:Rule val compose = createComposeRule()

    private class TestOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }

    private val owner = TestOwner()
    private var pauses = 0
    private var resumes = 0
    private var overCap = 0
    private var extraTimeBought = 0

    private val attempt = LessonAttempt(
        id = "intento",
        onRetry = {},
        submit = { _, _ -> LessonOutcome.SaveFailed },
        onPauseClock = { pauses++ },
        onResumeClock = { resumes++; overCap },
        onBuyExtraTime = { extraTimeBought++ },
        seedBalance = 600
    )

    private fun mission(timeLimit: Int) =
        MissionResponse(1, "Prueba", null, null, 1, 10, "matching", timeLimit, 3, true)

    private val pairs = listOf(MatchingPair(1, "Igualdad", "Mismos derechos"), MatchingPair(2, "Respeto", "Escuchar"))

    private fun moveTo(state: Lifecycle.State) {
        compose.runOnUiThread { owner.registry.currentState = state }
        compose.mainClock.advanceTimeByFrame()
    }

    private fun show(timeLimit: Int, initial: Lifecycle.State = Lifecycle.State.RESUMED) {
        compose.mainClock.autoAdvance = false
        compose.runOnUiThread { owner.registry.currentState = initial }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                NOVATheme(darkTheme = false) { MatchingLessonScreen(mission(timeLimit), pairs, 1, attempt, {}) }
            }
        }
        compose.mainClock.advanceTimeByFrame()
    }

    @Test fun losingFocusWhileVisibleKeepsTheClockRunning() {
        // Multiventana con el foco en la otra app, o un diálogo del sistema encima: solo ON_PAUSE.
        show(timeLimit = 60)
        moveTo(Lifecycle.State.STARTED)

        compose.mainClock.advanceTimeBy(3_000)

        compose.onNodeWithText("0:57").assertIsDisplayed()
        assertEquals(0, pauses)
    }

    @Test fun goingToBackgroundStopsTheClockAndChargesWhatPassedTheFreePause() {
        show(timeLimit = 60)
        moveTo(Lifecycle.State.CREATED)
        assertEquals(1, pauses)

        compose.mainClock.advanceTimeBy(10_000)
        compose.onNodeWithText("1:00").assertIsDisplayed()

        overCap = 5
        moveTo(Lifecycle.State.RESUMED)
        compose.onNodeWithText("0:55").assertIsDisplayed()
    }

    @Test fun comingBackAlwaysAsksTheViewModelEvenIfThisScreenMissedThePause() {
        // Android 7 y 8: el estado se guardó antes de ON_STOP y la pantalla
        // recreada no sabe que el ViewModel tiene el reloj en pausa.
        show(timeLimit = 60, initial = Lifecycle.State.CREATED)
        val before = resumes

        overCap = 7
        moveTo(Lifecycle.State.RESUMED)

        assertEquals(before + 1, resumes)
        compose.onNodeWithText("0:53").assertIsDisplayed()
    }

    @Test fun extraTimeAndRemainingSecondsSurviveRecreation() {
        compose.mainClock.autoAdvance = false
        compose.runOnUiThread { owner.registry.currentState = Lifecycle.State.RESUMED }
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                NOVATheme(darkTheme = false) { MatchingLessonScreen(mission(15), pairs, 1, attempt, {}) }
            }
        }
        compose.mainClock.advanceTimeByFrame()

        compose.onNodeWithText("+30 s · 500 semillas").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("0:45").assertIsDisplayed()

        restoration.emulateSavedInstanceStateRestore()
        compose.mainClock.advanceTimeByFrame()

        compose.onNodeWithText("0:45").assertIsDisplayed()
        compose.onNodeWithText("+30 s · 500 semillas").assertDoesNotExist()
        assertEquals(1, extraTimeBought)
    }
}

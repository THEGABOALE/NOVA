package com.mindflow.nova.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.mindflow.nova.data.local.NovaDatabase
import com.mindflow.nova.data.local.RoomLocalStore
import com.mindflow.nova.data.mission.MissionContentResult
import com.mindflow.nova.data.mission.MissionRepository
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.MissionOption
import com.mindflow.nova.data.model.MissionQuestion
import com.mindflow.nova.data.offline.AttemptRecorder
import com.mindflow.nova.data.offline.FinishedAttempt
import com.mindflow.nova.data.offline.LessonOutcome
import com.mindflow.nova.ui.components.ScopedViewModels
import com.mindflow.nova.ui.screens.lessons.LessonState
import com.mindflow.nova.ui.screens.lessons.LessonViewModel
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class ScopedLessonRestorationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun navigationOwnerRestoresSavedStateHandleInANewViewModel() {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, NovaDatabase::class.java).build()
        val local = RoomLocalStore(db.dao())
        val created = AtomicInteger()
        val attemptIds = AtomicInteger()
        val latest = AtomicReference<LessonViewModel>()
        val content = MissionContent(1, 1, "Prueba", null, null, 1, 10, "multiple_choice", null, 3,
            listOf(MissionQuestion(1, "Pregunta", "multiple_choice", null, 1, 1,
                listOf(MissionOption(1, "Sí", true, null, 1), MissionOption(2, "No", false, null, 2)))))
        val repository = object : MissionRepository {
            override suspend fun loadContent(missionId: Int) = MissionContentResult.Loaded(content)
            override suspend fun prefetch(missionIds: List<Int>) = Unit
        }
        val recorder = object : AttemptRecorder {
            override suspend fun record(attempt: FinishedAttempt) = LessonOutcome.SaveFailed
        }
        val factory = viewModelFactory {
            initializer {
                created.incrementAndGet()
                LessonViewModel(repository = repository, recorder = recorder,
                    savedState = createSavedStateHandle(), snapshotStore = local,
                    currentUserId = { 7 }, newId = { "attempt-${attemptIds.incrementAndGet()}" })
            }
        }
        val restoration = StateRestorationTester(compose)
        try {
            restoration.setContent {
                ScopedViewModels {
                    val vm: LessonViewModel = viewModel(factory = factory)
                    latest.set(vm)
                    val state by vm.state.collectAsState()
                    LaunchedEffect(Unit) { vm.open(1) }
                    Text((state as? LessonState.Playing)?.attemptId ?: "loading")
                }
            }
            compose.waitUntil(10_000) { latest.get()?.state?.value is LessonState.Playing }
            compose.onNodeWithText("attempt-1").assertIsDisplayed()
            restoration.emulateSavedInstanceStateRestore()
            compose.waitUntil(10_000) { created.get() > 1 && latest.get()?.state?.value is LessonState.Playing }
            compose.onNodeWithText("attempt-1").assertIsDisplayed()
            assertEquals(1, attemptIds.get())
        } finally { db.close() }
    }
}

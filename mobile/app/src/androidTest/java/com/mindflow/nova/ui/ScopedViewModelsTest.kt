package com.mindflow.nova.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindflow.nova.ui.components.ScopedViewModels
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Job
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ScopedViewModelsTest {
    @get:Rule val compose = createComposeRule()

    class ScopedProbe : ViewModel() {
        var cleared = false
        override fun onCleared() { cleared = true }
    }

    @Test fun keepsViewModelAliveWhileShownAndClearsItOnExit() {
        val probe = AtomicReference<ScopedProbe>()
        val shown = mutableStateOf(true)
        compose.setContent {
            val visible by shown
            if (visible) ScopedViewModels {
                probe.set(viewModel())
                Text("contenido")
            }
        }
        compose.waitForIdle()
        Thread.sleep(1_000)
        compose.waitForIdle()
        assertFalse(probe.get().cleared)
        assertFalse(probe.get().viewModelScope.coroutineContext[Job]!!.isCancelled)

        shown.value = false
        compose.waitForIdle()
        assertTrue(probe.get().cleared)
    }
}

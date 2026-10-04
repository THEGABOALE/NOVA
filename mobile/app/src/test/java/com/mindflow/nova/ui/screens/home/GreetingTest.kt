package com.mindflow.nova.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {

    @Test
    fun `saluda con el primer nombre`() {
        assertEquals("¡Hola, Gabriela!", greeting("Gabriela Ruiz Paz"))
        assertEquals("¡Hola, Ana!", greeting("  ana  "))
    }

    @Test
    fun `sin nombre saluda igual`() {
        assertEquals("¡Hola!", greeting(null))
        assertEquals("¡Hola!", greeting("   "))
    }
}

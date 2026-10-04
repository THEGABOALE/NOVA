package com.mindflow.nova.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `sin nada guardado sigue al sistema`() {
        assertEquals(ThemeMode.SYSTEM, themeModeFrom(stored = null, legacyDark = null))
    }

    @Test
    fun `el interruptor viejo se respeta`() {
        assertEquals(ThemeMode.DARK, themeModeFrom(stored = null, legacyDark = true))
        assertEquals(ThemeMode.LIGHT, themeModeFrom(stored = null, legacyDark = false))
    }

    @Test
    fun `lo guardado con la opción nueva manda`() {
        assertEquals(ThemeMode.LIGHT, themeModeFrom(stored = "LIGHT", legacyDark = true))
        assertEquals(ThemeMode.SYSTEM, themeModeFrom(stored = "cualquier_cosa", legacyDark = null))
    }

    @Test
    fun `oscuro según la opción y el teléfono`() {
        assertTrue(isDark(ThemeMode.SYSTEM, systemDark = true))
        assertFalse(isDark(ThemeMode.SYSTEM, systemDark = false))
        assertTrue(isDark(ThemeMode.DARK, systemDark = false))
        assertFalse(isDark(ThemeMode.LIGHT, systemDark = true))
    }
}

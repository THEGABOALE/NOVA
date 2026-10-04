package com.mindflow.nova.ui.screens.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTextTest {

    @Test
    fun `el texto cambia con el avance`() {
        assertEquals("Tu ruta empieza con la primera misión.", progressHeadline(0))
        assertEquals("Vas por buen camino: ya completaste el 40 % de tu nivel.", progressHeadline(40))
        assertEquals("¡Completaste tu nivel! Repasa cuando quieras.", progressHeadline(100))
    }

    @Test
    fun `fuera de rango se ajusta`() {
        assertEquals("Tu ruta empieza con la primera misión.", progressHeadline(-5))
        assertEquals("¡Completaste tu nivel! Repasa cuando quieras.", progressHeadline(130))
    }
}

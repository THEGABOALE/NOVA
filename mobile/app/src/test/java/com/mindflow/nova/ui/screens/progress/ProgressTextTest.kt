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

    @Test
    fun `la racha se cuenta en singular o plural, y sin días invita a empezar`() {
        assertEquals("Empieza tu racha completando una misión hoy.", streakLine(0))
        assertEquals("1 día de racha", streakLine(1))
        assertEquals("5 días de racha", streakLine(5))
    }
}

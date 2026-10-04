package com.mindflow.nova.ui.screens.progress

/** Frase de la tarjeta del nivel en Progreso, según cuánto lleva (0 a 100). */
internal fun progressHeadline(percentage: Int): String = when {
    percentage <= 0 -> "Tu ruta empieza con la primera misión."
    percentage >= 100 -> "¡Completaste tu nivel! Repasa cuando quieras."
    else -> "Vas por buen camino: ya completaste el $percentage % de tu nivel."
}

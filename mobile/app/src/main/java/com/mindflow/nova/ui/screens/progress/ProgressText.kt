package com.mindflow.nova.ui.screens.progress

/** Frase de la tarjeta del nivel en Progreso, según cuánto lleva (0 a 100). */
internal fun progressHeadline(percentage: Int): String = when {
    percentage <= 0 -> "Tu ruta empieza con la primera misión."
    percentage >= 100 -> "¡Completaste tu nivel! Repasa cuando quieras."
    else -> "Vas por buen camino: ya completaste el $percentage % de tu nivel."
}

/** Texto junto a la racha: los días, o una invitación si todavía no tiene. */
internal fun streakLine(days: Int): String = when {
    days <= 0 -> "Empieza tu racha completando una misión hoy."
    days == 1 -> "1 día de racha"
    else -> "$days días de racha"
}

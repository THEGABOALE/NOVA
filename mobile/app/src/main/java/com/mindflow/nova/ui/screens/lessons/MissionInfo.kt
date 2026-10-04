package com.mindflow.nova.ui.screens.lessons

import com.mindflow.nova.data.model.MissionResponse

data class MissionState(val isCompleted: Boolean, val isUnlocked: Boolean)

/**
 * Desbloqueo secuencial: una misión se puede jugar si ya está completada, o si
 * la anterior en el orden ya lo está (la primera siempre se puede).
 */
fun computeMissionStates(
    missions: List<MissionResponse>,
    completedMissionIds: Set<Int>
): List<MissionState> {
    var previousCompleted = true

    return missions.map { mission ->
        val isCompleted = mission.id in completedMissionIds
        val isUnlocked = isCompleted || previousCompleted
        previousCompleted = isCompleted

        MissionState(isCompleted = isCompleted, isUnlocked = isUnlocked)
    }
}

/** La "actual" es la primera todavía sin completar que ya está desbloqueada; -1 si no queda ninguna. */
fun currentMissionIndex(states: List<MissionState>): Int =
    states.indexOfFirst { it.isUnlocked && !it.isCompleted }

/**
 * Ítem de la lista del mapa en el que tiene que abrir: la misión disponible.
 * El mapa pone el encabezado en el ítem 0 y las misiones de la última a la
 * primera (la ruta sube), así que la de índice i está en total - i. Sin
 * misión disponible (todas completadas) abre arriba.
 */
internal fun mapScrollIndex(total: Int, currentIndex: Int): Int =
    if (currentIndex < 0) 0 else total - currentIndex

fun mechanicLabel(mechanic: String?): String = when (mechanic) {
    "multiple_choice" -> "Opción múltiple"
    "matching" -> "Relaciona conceptos"
    "true_false" -> "Verdadero o falso"
    "word_search" -> "Sopa de letras"
    else -> "Misión"
}

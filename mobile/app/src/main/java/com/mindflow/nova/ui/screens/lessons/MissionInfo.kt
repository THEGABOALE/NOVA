package com.mindflow.nova.ui.screens.lessons

import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.ui.screens.lessons.common.LESSON_MAX_PLUMAS

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

/**
 * Lo que el diálogo de inicio cuenta antes de empezar, para decidir si
 * jugarla ahora: solo lo que se sabe (sin reloj no se habla de tiempo).
 */
internal fun missionFacts(mission: MissionResponse, isReplay: Boolean): List<String> {
    val plumas = mission.maxPlumas ?: LESSON_MAX_PLUMAS
    return listOfNotNull(
        // En "Relaciona conceptos" toda la misión es una sola pregunta con
        // varios pares: decir "1 pregunta" confunde, así que no se cuenta.
        mission.questionCount?.takeIf { mission.mechanic != "matching" }
            ?.let { if (it == 1) "1 pregunta" else "$it preguntas" },
        if (plumas == 1) "1 pluma" else "$plumas plumas",
        mission.timeLimitSeconds?.let { "%d:%02d de tiempo".format(it / 60, it % 60) },
        // El repaso paga menos que la primera vez, y cada vez menos.
        if (isReplay) "Repaso: suma menos semillas" else "+${mission.pointsReward} semillas"
    )
}

fun mechanicLabel(mechanic: String?): String = when (mechanic) {
    "multiple_choice" -> "Opción múltiple"
    "matching" -> "Relaciona conceptos"
    "true_false" -> "Verdadero o falso"
    "word_search" -> "Sopa de letras"
    else -> "Misión"
}

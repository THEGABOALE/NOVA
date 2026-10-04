package com.mindflow.nova.ui.theme

/** Cómo se ve la app: como el teléfono, siempre clara o siempre oscura. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * La opción guardada. Antes había un interruptor de modo oscuro (booleano):
 * si solo existe ese, se respeta lo que la persona eligió. Sin nada guardado,
 * la app sigue al teléfono.
 */
internal fun themeModeFrom(stored: String?, legacyDark: Boolean?): ThemeMode =
    when {
        stored != null -> ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
        legacyDark == true -> ThemeMode.DARK
        legacyDark == false -> ThemeMode.LIGHT
        else -> ThemeMode.SYSTEM
    }

internal fun isDark(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

package com.mindflow.nova.data.session

import android.content.Context
import android.content.SharedPreferences
import com.mindflow.nova.ui.theme.themeModeFrom
import com.mindflow.nova.ui.theme.ThemeMode

/**
 * Guarda cómo quiere ver la app (como el teléfono, clara u oscura). Va aparte de [SessionStorage]
 * porque no es un dato sensible y tiene que sobrevivir al cierre de sesión.
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun themeMode(): ThemeMode = themeModeFrom(
        stored = prefs.getString(KEY_THEME_MODE, null),
        // Antes era un interruptor: si solo está ese, se respeta lo que eligió.
        legacyDark = if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    )

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).remove(KEY_DARK_MODE).apply()
    }

    private companion object {
        const val PREFS_NAME = "nova_preferences"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_THEME_MODE = "theme_mode"
    }
}

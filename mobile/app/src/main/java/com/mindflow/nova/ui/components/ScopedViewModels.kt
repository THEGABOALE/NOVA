package com.mindflow.nova.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Los ViewModel que se pidan dentro de [content] viven solo mientras este
 * bloque esté en pantalla y se destruyen al salir. Sin esto quedarían vivos
 * toda la vida de la Activity (la app no usa una librería de navegación que
 * los limpie), y cada lección abierta dejaría el suyo ocupando memoria.
 */
@Composable
fun ScopedViewModels(content: @Composable () -> Unit) {
    // NavBackStackEntry aporta SavedStateRegistry + CreationExtras, además
    // del ViewModelStore. El dueño anterior no podía restaurar SavedStateHandle.
    val controller = rememberNavController()
    val entry by controller.currentBackStackEntryAsState()
    val store by rememberUpdatedState(entry?.viewModelStore)
    val activity = LocalActivity.current
    // Se limpia solo cuando el bloque sale de pantalla. Atarlo a cada emisión
    // de la entrada limpiaba el ViewModel recién creado mientras seguía en uso.
    DisposableEffect(Unit) {
        onDispose {
            if (activity?.isChangingConfigurations != true) store?.clear()
        }
    }
    NavHost(navController = controller, startDestination = "scope") {
        composable("scope") { content() }
    }
}

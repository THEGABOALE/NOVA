package com.mindflow.nova.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.session.SessionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A dónde mandar a la persona:
 * - sin sesión válida -> login
 * - estudiante sin sala todavía -> onboarding (splash + código)
 * - estudiante con sala -> inicio del estudiante
 * - docente -> salas del docente
 * - coordinador/admin -> todavía no tienen pantalla (falta su wireframe)
 */
internal sealed class AppScreen {
    object Loading : AppScreen()
    object Login : AppScreen()
    object Onboarding : AppScreen()
    object StudentHome : AppScreen()
    object TeacherHome : AppScreen()
    data class Unsupported(val role: String) : AppScreen()

    /** Hay sesión guardada pero no se pudo validar (sin red o backend caído): se reintenta, no se cierra. */
    object ConnectionError : AppScreen()
}

internal fun routeForUser(user: SessionUser): AppScreen = when (user.role) {
    "student" -> if (user.group != null) AppScreen.StudentHome else AppScreen.Onboarding
    "teacher" -> AppScreen.TeacherHome
    else -> AppScreen.Unsupported(user.role)
}

/**
 * Guarda la pantalla ya decidida a nivel de Activity. Al girar el teléfono o
 * cambiar el tamaño de la ventana, Android recrea la Activity pero conserva
 * este ViewModel: la persona sigue donde estaba, sin volver a "Cargando" ni
 * repetir GET /api/auth/me. Si Android mata el proceso, el ViewModel se pierde
 * y el proceso nuevo vuelve a validar el token, como al abrir la app.
 */
internal class AppRouteViewModel(
    private val restoreSession: suspend () -> SessionResult
) : ViewModel() {

    private val _screen = MutableStateFlow<AppScreen>(AppScreen.Loading)
    val screen = _screen.asStateFlow()

    init {
        validate()
    }

    /** Valida el token guardado y elige la pantalla. Lo usa el arranque y "Reintentar". */
    fun validate() {
        _screen.value = AppScreen.Loading
        viewModelScope.launch {
            _screen.value = when (val result = restoreSession()) {
                is SessionResult.Success -> routeForUser(result.user)
                is SessionResult.Failure -> AppScreen.ConnectionError
                is SessionResult.Rejected -> AppScreen.Login
            }
        }
    }

    fun show(screen: AppScreen) {
        _screen.value = screen
    }
}

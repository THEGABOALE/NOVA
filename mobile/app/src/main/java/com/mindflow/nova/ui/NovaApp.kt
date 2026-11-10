package com.mindflow.nova.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.key
import com.mindflow.nova.ui.components.ScopedViewModels
import androidx.compose.runtime.Composable
import com.mindflow.nova.ui.theme.isDark
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.mindflow.nova.AppServices
import com.mindflow.nova.data.session.SessionRepository
import com.mindflow.nova.data.session.ThemePreferences
import com.mindflow.nova.ui.screens.auth.LoginScreen
import com.mindflow.nova.ui.screens.auth.OnboardingScreen
import com.mindflow.nova.ui.screens.home.HomeScreen
import com.mindflow.nova.ui.screens.teacher.TeacherRoomsScreen
import com.mindflow.nova.ui.theme.NovaOnPurple
import com.mindflow.nova.ui.theme.NOVATheme
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

/**
 * Raíz de la app. Al arrancar el proceso valida el token guardado contra el
 * backend (GET /api/auth/me) y decide a dónde mandar a la persona (ver
 * [AppScreen]): [LoginScreen], [OnboardingScreen], [HomeScreen] o
 * [TeacherRoomsScreen]. La pantalla decidida vive en [AppRouteViewModel], así
 * que recrear la Activity no repite la validación.
 */
@Composable
fun NovaApp(session: SessionRepository, themePreferences: ThemePreferences) {
    val route: AppRouteViewModel = viewModel { AppRouteViewModel(session::restoreSession) }
    val screen by route.screen.collectAsState()
    var themeMode by remember { mutableStateOf(themePreferences.themeMode()) }

    // El tema elegido (o el del teléfono) se aplica al estudiante y al docente.
    // Login y onboarding quedan en claro: su arte de ondas es sobre fondo claro.
    val themedScreen = screen is AppScreen.StudentHome || screen is AppScreen.TeacherHome
    val darkActive = themedScreen && isDark(themeMode, isSystemInDarkTheme())

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        // Sobre el arte oscuro del login los iconos de la barra de estado van claros;
        // sobre las demás (el onboarding tiene fondo claro), oscuros en claro y claros en oscuro.
        val lightIcons = screen is AppScreen.Login || darkActive
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !lightIcons
    }

    NOVATheme(darkTheme = darkActive) {
        Box(Modifier.fillMaxSize().background(NovaBackground), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 720.dp).fillMaxSize()) {
        when (val current = screen) {
            AppScreen.Loading -> LoadingScreen()

            AppScreen.Login -> LoginScreen(
                session = session,
                onLoginSuccess = { user ->
                    // Si esta cuenta dejó resultados sin subir en este teléfono, se suben ahora.
                    AppServices.scheduleSync()
                    route.show(routeForUser(user))
                }
            )

            AppScreen.Onboarding -> OnboardingScreen(
                onJoined = { route.show(AppScreen.StudentHome) },
                onLogout = {
                    session.logout()
                    route.show(AppScreen.Login)
                }
            )

            AppScreen.StudentHome -> key(session.currentUserId()) { ScopedViewModels { HomeScreen(
                themeMode = themeMode,
                onThemeModeChange = { mode ->
                    themeMode = mode
                    themePreferences.setThemeMode(mode)
                },
                onLogout = {
                    session.logout()
                    route.show(AppScreen.Login)
                }
            ) } }

            AppScreen.TeacherHome -> key(session.currentUserId()) { ScopedViewModels { TeacherRoomsScreen(
                onLogout = {
                    session.logout()
                    route.show(AppScreen.Login)
                }
            ) } }

            AppScreen.ConnectionError -> ConnectionErrorScreen(
                onRetry = route::validate,
                onLogout = {
                    session.logout()
                    route.show(AppScreen.Login)
                }
            )

            is AppScreen.Unsupported -> UnsupportedRoleScreen(
                role = current.role,
                onLogout = {
                    session.logout()
                    route.show(AppScreen.Login)
                }
            )
        }
        }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(NovaBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = NovaPurple)
    }
}

@Composable
private fun ConnectionErrorScreen(onRetry: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CloudOff,
            contentDescription = null,
            tint = NovaPurple,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No pudimos conectar con NOVA",
            color = NovaText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Revisa tu conexión a internet e intenta de nuevo. Tu sesión sigue guardada.",
            color = NovaTextSecondary,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaPurple,
                contentColor = NovaOnPurple
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = "Reintentar", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onLogout) {
            Text(
                text = "Cerrar sesión",
                color = NovaTextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Coordinador y admin todavía no tienen wireframe de pantalla propia.
@Composable
private fun UnsupportedRoleScreen(role: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(120.dp))

        Text(
            text = "NOVA",
            color = NovaPurple,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Todavía no hay una pantalla para el rol \"$role\".",
            color = NovaTextSecondary,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Cerrar sesión")
        }
    }
}

package com.mindflow.nova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mindflow.nova.ui.NovaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Antes de super: cambia del tema de arranque (Theme.NOVA.Starting) al normal.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val app = application as NovaApplication

        setContent {
            NovaApp(session = app.session, themePreferences = app.themePreferences)
        }
    }
}

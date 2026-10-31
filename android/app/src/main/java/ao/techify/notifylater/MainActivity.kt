package ao.techify.notifylater

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ao.techify.notifylater.feature.splash.SplashScreen
import ao.techify.notifylater.navigation.AppShell
import ao.techify.notifylater.navigation.Routes
import ao.techify.notifylater.ui.theme.NotifyLaterTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = newBase.getSharedPreferences("notify_later_settings", Context.MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = applicationContext as NotifyLaterApplicationHolder
        val startDestination = if (app.settingsRepository.current.onboardingCompleted) {
            Routes.HOME
        } else {
            Routes.ONBOARDING
        }

        // Splash only on a cold start; not after rotation or language change.
        val coldStart = savedInstanceState == null

        setContent {
            NotifyLaterTheme {
                var showSplash by rememberSaveable { mutableStateOf(coldStart) }
                val navController = rememberNavController()
                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(450),
                    label = "splash"
                ) { splash ->
                    if (splash) {
                        SplashScreen(onFinished = { showSplash = false })
                    } else {
                        AppShell(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}

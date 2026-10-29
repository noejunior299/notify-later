package ao.techify.notifylater

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
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

        setContent {
            NotifyLaterTheme {
                val navController = rememberNavController()
                AppShell(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}

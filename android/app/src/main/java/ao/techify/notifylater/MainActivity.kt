package ao.techify.notifylater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import ao.techify.notifylater.navigation.AppShell
import ao.techify.notifylater.navigation.Routes
import ao.techify.notifylater.ui.theme.NotifyLaterTheme

class MainActivity : ComponentActivity() {
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

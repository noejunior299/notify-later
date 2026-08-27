package ao.techify.notifylater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import ao.techify.notifylater.navigation.AppNavHost
import ao.techify.notifylater.ui.theme.NotifyLaterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotifyLaterTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}

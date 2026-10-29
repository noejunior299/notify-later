package ao.techify.notifylater.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ao.techify.notifylater.NotifyLaterApplicationHolder
import ao.techify.notifylater.feature.applications.ApplicationDetailsScreen
import ao.techify.notifylater.feature.applications.ApplicationsScreen
import ao.techify.notifylater.feature.home.HomeScreen
import ao.techify.notifylater.feature.logs.LogsScreen
import ao.techify.notifylater.feature.onboarding.OnboardingScreen
import ao.techify.notifylater.feature.queue.QueueScreen
import ao.techify.notifylater.feature.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.ONBOARDING
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(durationMillis = 200)) },
        exitTransition = { fadeOut(tween(durationMillis = 140)) }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    (context.applicationContext as NotifyLaterApplicationHolder)
                        .completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToApplications = { navController.navigate(Routes.APPLICATIONS) },
                onNavigateToQueue = { navController.navigate(Routes.QUEUE) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToLogs = { navController.navigate(Routes.LOGS) }
            )
        }
        composable(Routes.APPLICATIONS) {
            ApplicationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { pkg -> navController.navigate(Routes.applicationDetails(pkg)) }
            )
        }
        composable(
            route = Routes.APPLICATION_DETAILS_WITH_ARG,
            arguments = listOf(navArgument(Routes.ARG_PACKAGE_NAME) { type = NavType.StringType })
        ) { backStackEntry ->
            val pkg = backStackEntry.arguments?.getString(Routes.ARG_PACKAGE_NAME).orEmpty()
            ApplicationDetailsScreen(
                packageName = pkg,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.QUEUE) {
            QueueScreen()
        }
        composable(Routes.LOGS) {
            LogsScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToLogs = { navController.navigate(Routes.LOGS) }
            )
        }
    }
}

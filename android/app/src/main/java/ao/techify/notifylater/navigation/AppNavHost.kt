package ao.techify.notifylater.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
    startDestination: String = Routes.ONBOARDING
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
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
            QueueScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.LOGS) {
            LogsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogs = { navController.navigate(Routes.LOGS) }
            )
        }
    }
}

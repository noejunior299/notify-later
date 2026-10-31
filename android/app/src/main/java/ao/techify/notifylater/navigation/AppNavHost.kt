package ao.techify.notifylater.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import ao.techify.notifylater.feature.about.AboutScreen
import ao.techify.notifylater.feature.about.PrivacyScreen
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
    onNavigateToTab: (String) -> Unit = { navController.navigate(it) },
    startDestination: String = Routes.ONBOARDING
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(260, delayMillis = 60)) +
                scaleIn(tween(300, easing = FastOutSlowInEasing), initialScale = 0.96f)
        },
        exitTransition = { fadeOut(tween(140)) },
        popEnterTransition = {
            fadeIn(tween(260, delayMillis = 60)) +
                scaleIn(tween(300, easing = FastOutSlowInEasing), initialScale = 1.03f)
        },
        popExitTransition = {
            fadeOut(tween(140)) + scaleOut(tween(200), targetScale = 0.96f)
        }
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
                onNavigateToQueue = { onNavigateToTab(Routes.QUEUE) },
                onNavigateToSettings = { onNavigateToTab(Routes.SETTINGS) },
                onNavigateToLogs = { onNavigateToTab(Routes.LOGS) }
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
                onNavigateToLogs = { onNavigateToTab(Routes.LOGS) },
                onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY) }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY) }
            )
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

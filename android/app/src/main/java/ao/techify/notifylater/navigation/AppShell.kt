package ao.techify.notifylater.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import ao.techify.notifylater.R

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, R.string.nav_home, Icons.Rounded.Home),
    TopLevelDestination(Routes.QUEUE, R.string.nav_queue, Icons.Rounded.Schedule),
    TopLevelDestination(Routes.LOGS, R.string.nav_logs, Icons.Rounded.History),
    TopLevelDestination(Routes.SETTINGS, R.string.nav_settings, Icons.Rounded.Settings)
)

/**
 * Shell da aplicação: as quatro áreas principais vivem numa barra de navegação
 * Material 3. Ecrãs secundários (onboarding, aplicações) escondem a barra.
 */
@Composable
fun AppShell(
    navController: NavHostController,
    startDestination: String = Routes.ONBOARDING
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) navController.navigateToTab(destination.route)
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        AppNavHost(
            navController = navController,
            onNavigateToTab = { route -> navController.navigateToTab(route) },
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            startDestination = startDestination
        )
    }
}

/**
 * Navegação entre separadores.
 *
 * Home é a raiz: voltar a ela é sempre um pop até ela (nunca um navigate com
 * restoreState, que restaurava a pilha guardada da tab anterior e fazia o
 * toque em Home "não fazer nada"). As outras tabs ficam por cima de Home,
 * uma de cada vez, preservando o estado de cada uma.
 */
fun NavHostController.navigateToTab(route: String) {
    if (route == Routes.HOME) {
        if (!popBackStack(Routes.HOME, inclusive = false)) {
            navigate(Routes.HOME) {
                popUpTo(graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
        return
    }
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

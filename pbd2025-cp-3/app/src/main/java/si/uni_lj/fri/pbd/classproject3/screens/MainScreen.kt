package si.uni_lj.fri.pbd.classproject3.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import si.uni_lj.fri.pbd.classproject3.navigation.AppNavigation
import si.uni_lj.fri.pbd.classproject3.navigation.BottomNavItem
import si.uni_lj.fri.pbd.classproject3.viewmodels.ViewModelFactory

/**
 * The main screen of the application, hosting the Scaffold with bottom navigation.
 *
 * @param factory The ViewModelFactory for creating ViewModels, passed down to AppNavigation.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Will be used by NavHost
@Composable
fun MainScreen(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Search,
        BottomNavItem.Favorites
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // AppNavigation will host the different screens
        AppNavigation(
            navController = navController,
            factory = factory,
            // Pass the padding to NavHost, so content is not obscured by system bars or bottom nav
            // Modifier.padding(innerPadding) should be applied to the NavHost container if necessary,
            // or to individual screens if they need to handle padding themselves.
            // For now, AppNavigation itself doesn't take a modifier, but its composables might.
            // The NavHost inside AppNavigation will be placed in the content area of the Scaffold.
        )
        // The NavHost in AppNavigation is the content of the Scaffold.
        // It implicitly uses the space provided by the Scaffold's content lambda (it/innerPadding).
        // If AppNavigation's NavHost needed explicit padding from the Scaffold,
        // you would pass innerPadding to it.
    }
}
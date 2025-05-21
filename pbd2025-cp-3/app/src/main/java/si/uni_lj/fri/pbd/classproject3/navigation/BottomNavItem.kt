package si.uni_lj.fri.pbd.classproject3.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing items in the bottom navigation bar.
 * Each item has a route, an icon, and a title resource ID.
 */
sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Search : BottomNavItem(
        route = "search_screen",
        icon = Icons.Filled.Search,
        title = "Search"
    )

    object Favorites : BottomNavItem(
        route = "favorites_screen",
        icon = Icons.Filled.Favorite,
        title = "Favorites"
    )
    // RecipeDetailsScreen is not a bottom nav item, it's navigated to from Search/Favorites
}
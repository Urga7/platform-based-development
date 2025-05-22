package si.uni_lj.fri.pbd.classproject3.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import si.uni_lj.fri.pbd.classproject3.screens.FavoritesScreen
import si.uni_lj.fri.pbd.classproject3.screens.RecipeDetailsScreen
import si.uni_lj.fri.pbd.classproject3.screens.SearchScreen
import si.uni_lj.fri.pbd.classproject3.viewmodels.DetailsViewModel
import si.uni_lj.fri.pbd.classproject3.viewmodels.FavoritesViewModel
import si.uni_lj.fri.pbd.classproject3.viewmodels.SearchViewModel
import si.uni_lj.fri.pbd.classproject3.viewmodels.ViewModelFactory

/**
 * Defines the navigation graph for the application.
 *
 * @param navController The NavHostController for managing navigation.
 * @param factory The ViewModelFactory for creating ViewModels.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    factory: ViewModelFactory
) {
    NavHost(navController = navController, startDestination = BottomNavItem.Search.route) {
        // Search Screen
        composable(BottomNavItem.Search.route) {
            val searchViewModel: SearchViewModel = viewModel(factory = factory)
            SearchScreen(
                searchViewModel = searchViewModel,
                onRecipeClick = { recipeId ->
                    // Navigate to RecipeDetailsScreen from Search, passing recipeId
                    // and indicating it's from search (for API fetch)
                    navController.navigate("recipe_details_screen/$recipeId/true")
                }
            )
        }

        // Favorites Screen
        composable(BottomNavItem.Favorites.route) {
            val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)
            FavoritesScreen(
                favoritesViewModel = favoritesViewModel,
                onRecipeClick = { recipeId ->
                    // Navigate to RecipeDetailsScreen from Favorites, passing recipeId
                    // and indicating it's from favorites (for DB fetch)
                    navController.navigate("recipe_details_screen/$recipeId/false")
                }
            )
        }

        // Recipe Details Screen
        composable(
            route = "recipe_details_screen/{recipeId}/{fromSearch}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType },
                navArgument("fromSearch") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            val fromSearch = backStackEntry.arguments?.getBoolean("fromSearch")
            val detailsViewModel: DetailsViewModel = viewModel(factory = factory)

            if (recipeId == null || fromSearch == null) {
                // Missing arguments
                navController.popBackStack()
                return@composable
            }

            RecipeDetailsScreen(
                recipeId = recipeId,
                fromSearchScreen = fromSearch,
                detailsViewModel = detailsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
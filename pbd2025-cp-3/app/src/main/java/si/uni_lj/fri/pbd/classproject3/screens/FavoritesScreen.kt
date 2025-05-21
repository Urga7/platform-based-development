package si.uni_lj.fri.pbd.classproject3.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import si.uni_lj.fri.pbd.classproject3.viewmodels.FavoritesViewModel
import si.uni_lj.fri.pbd.classproject3.viewmodels.FavoritesUiState

@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    onRecipeClick: (recipeId: String) -> Unit
) {
    val uiState by favoritesViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle showing snackbar for errors (if any are added to FavoritesUiState)
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            // favoritesViewModel.errorMessageShown() // Add this if you implement error clearing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues -> // Padding from Scaffold (e.g., if there was a TopAppBar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp, vertical = 8.dp) // Outer padding for screen content
        ) {
            Text(
                text = "Your Favorite Recipes",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.favoriteRecipes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "You haven't added any favorite recipes yet. \nStar some recipes in the search screen!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // Two columns
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.favoriteRecipes, key = { it.idMeal }) { recipe ->
                            // Reusing the RecipeGridItem from SearchScreen
                            RecipeGridItem(recipe = recipe, onClick = { onRecipeClick(recipe.idMeal) })
                        }
                    }
                }
            }
        }
    }
}
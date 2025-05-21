package si.uni_lj.fri.pbd.classproject3.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import si.uni_lj.fri.pbd.classproject3.R
import si.uni_lj.fri.pbd.classproject3.models.dto.IngredientDTO
import si.uni_lj.fri.pbd.classproject3.models.RecipeSummaryIM
import si.uni_lj.fri.pbd.classproject3.viewmodels.SearchUiState
import si.uni_lj.fri.pbd.classproject3.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    onRecipeClick: (recipeId: String) -> Unit
) {
    val uiState by searchViewModel.uiState.collectAsState()
    var selectedIngredientName by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Swipe to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoadingRecipes,
        onRefresh = {
            selectedIngredientName?.let {
                searchViewModel.fetchRecipesByIngredient(it, forceRefresh = true)
            }
        }
    )

    // Handle showing snackbar for errors
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            searchViewModel.errorMessageShown() // Reset error message
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues -> // Padding from Scaffold itself, if any top app bar was present
        Box(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Outer padding for the screen content
            ) {
                // Ingredients Dropdown
                if (uiState.isLoadingIngredients) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState.ingredients.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedIngredientName ?: "Select an Ingredient",
                            onValueChange = {}, // Not directly editable
                            readOnly = true,
                            label = { Text("Main Ingredient") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            uiState.ingredients.forEach { ingredient ->
                                ingredient.strIngredient?.let { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            selectedIngredientName = name
                                            searchViewModel.fetchRecipesByIngredient(name)
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // This case (no ingredients and not loading) might indicate an initial load error for ingredients
                    if (uiState.errorMessage == null && !uiState.isLoadingIngredients) { // Avoid double message
                        Text(
                            "Could not load ingredients. Swipe down to retry or check connection.",
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recipes Grid / Loading / Messages
                when {
                    uiState.isLoadingRecipes && uiState.recipes.isEmpty() -> { // Show central loader only if recipes list is empty
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.noRecipesMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(uiState.noRecipesMessage!!, textAlign = TextAlign.Center)
                        }
                    }
                    uiState.recipes.isNotEmpty() -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2), // Two columns
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.recipes, key = { it.idMeal }) { recipe ->
                                RecipeGridItem(recipe = recipe, onClick = { onRecipeClick(recipe.idMeal) })
                            }
                        }
                    }
                    selectedIngredientName != null && !uiState.isLoadingRecipes && uiState.errorMessage == null && uiState.noRecipesMessage == null -> {
                        // This state means an ingredient is selected, not loading, no specific error/no-recipe message, but recipes are empty.
                        // This could happen if an API call for recipes by ingredient returns null/empty without triggering specific messages.
                        // Or if an ingredient was selected but fetchRecipesByIngredient hasn't populated recipes yet.
                        // Usually covered by isLoadingRecipes or noRecipesMessage.
                        // For safety, can add a generic prompt or leave it to be handled by other states.
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoadingRecipes,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun RecipeGridItem(
    recipe: RecipeSummaryIM,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.strMealThumb)
                    .crossfade(true)
                    // You should have placeholder and error drawables in your res/drawable
                    .placeholder(R.drawable.ic_placeholder_image) // Create a placeholder drawable
                    .error(R.drawable.ic_error_image) // Create an error drawable
                    .build(),
                contentDescription = recipe.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square image
            )
            Text(
                text = recipe.strMeal,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
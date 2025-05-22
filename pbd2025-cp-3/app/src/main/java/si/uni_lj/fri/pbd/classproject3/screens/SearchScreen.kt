package si.uni_lj.fri.pbd.classproject3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import si.uni_lj.fri.pbd.classproject3.screens.common.RecipeGridItem
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

    // Determine if any loading operation relevant to pull-to-refresh is active
    val isActuallyRefreshing = uiState.isLoadingIngredients || (selectedIngredientName != null && uiState.isLoadingRecipes)

    // Swipe to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isActuallyRefreshing,
        onRefresh = {
            if (selectedIngredientName != null && uiState.ingredients.isNotEmpty()) { // Ensure ingredients were loaded before trying to refresh recipes
                searchViewModel.fetchRecipesByIngredient(selectedIngredientName!!, forceRefresh = true)
            } else {
                // If no ingredient selected, or if ingredients list is empty (failed initial load), refresh ingredients.
                searchViewModel.fetchIngredients()
            }
        }
    )

    // Handle showing snackbar for errors
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            searchViewModel.errorMessageShown() // Reset error message
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box( // This Box has the pullRefresh modifier
            modifier = Modifier
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            // Determine if the content will be the LazyVerticalGrid
            val shouldShowRecipeGrid = uiState.recipes.isNotEmpty() && selectedIngredientName != null

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent) // For consistent touch behavior
                    // Apply verticalScroll only if not displaying the LazyVerticalGrid.
                    // This makes the Column scrollable for static messages/empty states.
                    .then(if (!shouldShowRecipeGrid) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ingredients Dropdown Area
                if (uiState.isLoadingIngredients && selectedIngredientName == null && uiState.ingredients.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 24.dp))
                } else if (uiState.ingredients.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedIngredientName ?: "Select an Ingredient",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Main Ingredient") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                                            searchViewModel.fetchRecipesByIngredient(name, forceRefresh = false)
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (!uiState.isLoadingIngredients && uiState.errorMessage == null) {
                    // This is the "Could not load ingredients" state.
                    // The Column is scrollable here due to !shouldShowRecipeGrid.
                    // Add a Spacer with weight to push this message down if the Column needs to fill height.
                    Spacer(modifier = Modifier.weight(0.2f)) // Pushes content down a bit
                    Text(
                        "Could not load ingredients. Swipe down to retry or check connection.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(modifier = Modifier.weight(0.8f)) // Fills remaining space to ensure scrollability
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Area: Recipes Grid or Messages
                // This Box will fill the remaining vertical space if the Column is not scrollable (i.e., when grid is shown)
                // or be part of the scrollable content if the Column is scrollable.
                Box(
                    modifier = Modifier
                        .weight(1f) // Ensures this section tries to take available space
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedIngredientName != null && uiState.isLoadingRecipes && uiState.recipes.isEmpty() -> {
                            CircularProgressIndicator()
                        }
                        uiState.noRecipesMessage != null -> {
                            Text(uiState.noRecipesMessage!!, textAlign = TextAlign.Center)
                        }
                        shouldShowRecipeGrid -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize() // Grid fills the space given by this Box
                            ) {
                                items(uiState.recipes, key = { it.idMeal }) { recipe ->
                                    RecipeGridItem(recipe = recipe, onClick = { onRecipeClick(recipe.idMeal) })
                                }
                            }
                        }
                        selectedIngredientName != null && !uiState.isLoadingRecipes && uiState.recipes.isEmpty() && uiState.noRecipesMessage == null && uiState.errorMessage == null -> {
                            Text(
                                "No recipes found for '$selectedIngredientName'.\nPull down to refresh or select another ingredient.",
                                textAlign = TextAlign.Center
                            )
                        }
                        // Default empty state for this Box if no other condition met
                        // (e.g., no ingredient selected yet, and ingredients did load).
                        // The parent Column's scroll + weighted spacers handle the overall scrollability.
                        else -> {
                            if (uiState.ingredients.isNotEmpty() && selectedIngredientName == null && !uiState.isLoadingIngredients) {
                                Text("Select an ingredient to see recipes.", textAlign = TextAlign.Center)
                            }
                            // Implicitly, this Box with weight(1f) helps the scrollable Column have extent.
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isActuallyRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
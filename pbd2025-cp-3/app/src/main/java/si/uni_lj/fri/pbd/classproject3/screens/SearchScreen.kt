package si.uni_lj.fri.pbd.classproject3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
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
    ) { scaffoldPaddingValues ->
        Box( // This Box has the pullRefresh modifier
            modifier = Modifier
                .padding(scaffoldPaddingValues)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            // Determine if the content will be the LazyVerticalGrid
            val shouldShowRecipeGrid = uiState.recipes.isNotEmpty() && selectedIngredientName != null

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
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
                    // The Column is scrollable here due to !shouldShowRecipeGrid.
                    Spacer(modifier = Modifier.weight(0.2f)) // Pushes content down a bit
                    Text(
                        "Could not load ingredients. Check your connection and swipe down to retry.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(modifier = Modifier.weight(0.8f)) // Fills remaining space to ensure scrollability
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Area: Recipes Grid or Messages
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                                contentPadding = PaddingValues(
                                    top = 8.dp,
                                    bottom = 64.dp + scaffoldPaddingValues.calculateBottomPadding()
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
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

                        else -> {
                            if (uiState.ingredients.isNotEmpty() && selectedIngredientName == null && !uiState.isLoadingIngredients) {
                                Text("Select an ingredient to see recipes.", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}
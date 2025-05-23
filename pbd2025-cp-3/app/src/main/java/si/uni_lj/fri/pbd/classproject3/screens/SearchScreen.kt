package si.uni_lj.fri.pbd.classproject3.screens

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
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val isActuallyRefreshing = uiState.isLoadingIngredients || (uiState.selectedIngredient != null && uiState.isLoadingRecipes)

    // Swipe to refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isActuallyRefreshing,
        onRefresh = {
            if (uiState.selectedIngredient != null && uiState.ingredients.isNotEmpty()) {
                searchViewModel.fetchRecipesByIngredient(uiState.selectedIngredient!!, forceRefresh = true)
            } else {
                searchViewModel.fetchIngredients()
            }
        }
    )

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            searchViewModel.errorMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPaddingValues ->
        Box(
            modifier = Modifier
                .padding(scaffoldPaddingValues)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
        ) {
            when {
                // Case 1: Initial ingredients loading phase
                uiState.isLoadingIngredients && uiState.ingredients.isEmpty() && uiState.selectedIngredient == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Case 2: Failed to load ingredients initially
                !uiState.isLoadingIngredients && uiState.ingredients.isEmpty() && uiState.selectedIngredient == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()) // Scrollable for pull-refresh
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Could not load ingredients. Check your connection and swipe down to retry.",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Case 3: Ingredients are loaded, or an ingredient is selected (normal operation)
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .then(
                                if (uiState.recipes.isEmpty() || uiState.selectedIngredient == null || !uiState.ingredients.isNotEmpty()) {
                                    Modifier.verticalScroll(rememberScrollState())
                                } else {
                                    Modifier
                                }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dropdown for ingredients (only if ingredients are available)
                        if (uiState.ingredients.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = isDropdownExpanded,
                                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = uiState.selectedIngredient ?: "Select an Ingredient",
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
                                                    searchViewModel.fetchRecipesByIngredient(name, forceRefresh = false)
                                                    isDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            // Consistent spacer after the dropdown if it's shown.
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val shouldShowRecipeGrid = uiState.recipes.isNotEmpty() && uiState.selectedIngredient != null
                            when {

                                // Loading recipes for a selected ingredient
                                uiState.selectedIngredient != null && uiState.isLoadingRecipes && uiState.recipes.isEmpty() -> {
                                    CircularProgressIndicator()
                                }

                                // Specific message from ViewModel for no recipes
                                uiState.noRecipesMessage != null -> {
                                    Text(
                                        uiState.noRecipesMessage!!,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                // Display recipe grid
                                shouldShowRecipeGrid -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(top = 0.dp, bottom = 64.dp + scaffoldPaddingValues.calculateBottomPadding()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(uiState.recipes, key = { it.idMeal }) { recipe ->
                                            RecipeGridItem(recipe = recipe, onClick = { onRecipeClick(recipe.idMeal) })
                                        }
                                    }
                                }

                                // Message when an ingredient is selected, but no recipes were found (after loading)
                                uiState.selectedIngredient != null && uiState.noRecipesMessage == null -> {
                                    Text(
                                        "No recipes found for '${uiState.selectedIngredient}'.",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                // Message to prompt selection if ingredients are loaded but none is selected yet
                                uiState.ingredients.isNotEmpty() && !uiState.isLoadingIngredients -> {
                                    Text(
                                        "Select an ingredient to see recipes.",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
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
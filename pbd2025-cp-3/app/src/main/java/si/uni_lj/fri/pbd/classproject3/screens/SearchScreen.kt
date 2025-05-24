package si.uni_lj.fri.pbd.classproject3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.lazy.items

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
                                    LazyColumn(modifier = Modifier.size(600.dp)) {
                                        items(uiState.ingredients) { ingredient ->
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
                            }
                            // Consistent spacer after the dropdown if it's shown.
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                // 1. Still loading recipes for the selected ingredient
                                uiState.selectedIngredient != null && uiState.isLoadingRecipes && uiState.recipes.isEmpty() -> {
                                    CircularProgressIndicator()
                                }

                                // 2. Display error message if present (e.g., "Connection lost...")
                                //    This takes precedence if an ingredient is selected, we're not loading, and an error occurred.
                                uiState.selectedIngredient != null && !uiState.isLoadingRecipes && uiState.errorMessage != null -> {
                                    Text(
                                        text = uiState.errorMessage!!, // This will show "Connection lost..."
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                // 3. Specific "no recipes" message from ViewModel (successful fetch, but API found nothing)
                                uiState.selectedIngredient != null && !uiState.isLoadingRecipes && uiState.noRecipesMessage != null -> {
                                    Text(
                                        uiState.noRecipesMessage!!,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                // 4. Recipes are loaded and available
                                uiState.selectedIngredient != null && uiState.recipes.isNotEmpty() -> {
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

                                // 5. Fallback: Ingredient selected, not loading, recipes empty, and no specific error/noRecipesMessage yet.
                                //    This typically means the initial state after selection before recipes load or a generic "not found".
                                uiState.selectedIngredient != null && !uiState.isLoadingRecipes && uiState.recipes.isEmpty()-> {
                                    Text(
                                        // This message will now be less likely to show if errorMessage handles the failure.
                                        "Could not load recipes for '${uiState.selectedIngredient}'. Please check your connection.",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }

                                // 6. Prompt to select an ingredient if ingredients are loaded but none is selected yet
                                uiState.ingredients.isNotEmpty() && !uiState.isLoadingIngredients && uiState.selectedIngredient == null -> {
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
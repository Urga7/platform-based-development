package si.uni_lj.fri.pbd.classproject3.viewmodels

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import si.uni_lj.fri.pbd.classproject3.models.dto.IngredientDTO
import si.uni_lj.fri.pbd.classproject3.models.RecipeSummaryIM
import si.uni_lj.fri.pbd.classproject3.repository.RecipeRepository

// Represents the state of the SearchScreen UI
data class SearchUiState(
    val ingredients: List<IngredientDTO> = emptyList(),
    val recipes: List<RecipeSummaryIM> = emptyList(),
    val isLoadingIngredients: Boolean = false,
    val isLoadingRecipes: Boolean = false,
    val selectedIngredient: String? = null,
    val errorMessage: String? = null, // For general errors
    val noRecipesMessage: String? = null // Specific message when no recipes found
)

class SearchViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var lastFetchTimeMillis: Long = 0
    private val fetchDebounceMillis: Long = 5000 // 5 seconds

    init { fetchIngredients() }

    /**
     * Fetches the list of ingredients from the repository.
     */
    fun fetchIngredients() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingIngredients = true, errorMessage = null)
            try {
                val ingredientsList = repository.getAllIngredients()
                _uiState.value = _uiState.value.copy(
                    ingredients = ingredientsList ?: emptyList(),
                    isLoadingIngredients = false
                )
                if (ingredientsList == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Could not load ingredients.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingIngredients = false,
                    errorMessage = "Error fetching ingredients: ${e.message}"
                )
            }
        }
    }

    /**
     * Fetches recipes for the given ingredient.
     * Includes a debounce mechanism for swipe-to-refresh.
     * @param ingredientName The name of the ingredient to search for.
     * @param forceRefresh If true, bypasses the debounce mechanism.
     */
    fun fetchRecipesByIngredient(ingredientName: String, forceRefresh: Boolean = false) {
        val currentTime = SystemClock.elapsedRealtime()
        if (!forceRefresh && (currentTime - lastFetchTimeMillis < fetchDebounceMillis) && ingredientName == _uiState.value.selectedIngredient) {
            // Debounce: Too soon since last fetch for the same ingredient
            _uiState.value = _uiState.value.copy(isLoadingRecipes = false) // Ensure loading is off if skipped
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingRecipes = true,
                selectedIngredient = ingredientName,
                recipes = emptyList(), // Clear previous recipes
                errorMessage = null,
                noRecipesMessage = null
            )
            try {
                val recipesList = repository.getRecipesByIngredient(ingredientName)
                Log.d("SearchViewModel", recipesList.toString())
                if (recipesList == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Could not load recipes for '$ingredientName'.",
                        isLoadingRecipes = false,
                        recipes = emptyList()
                    )
                } else if (recipesList.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        noRecipesMessage = "Sorry, no recipes for '$ingredientName' exist.",
                        isLoadingRecipes = false,
                        recipes = emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        recipes = recipesList,
                        isLoadingRecipes = false
                    )
                }

                lastFetchTimeMillis = SystemClock.elapsedRealtime()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingRecipes = false,
                    errorMessage = "Error fetching recipes: ${e.message}",
                    recipes = emptyList()
                )
            }
        }
    }

    /**
     * Called when an error message has been shown and should be cleared.
     */
    fun errorMessageShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Pre-populates the database with some random recipes.
     * This is intended to be called from SplashScreen.
     */
    fun prepopulateDatabase() {
        viewModelScope.launch {
            repository.prepopulateDatabaseWithRandomRecipes()
        }
    }
}
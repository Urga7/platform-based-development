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
import java.io.IOException // Import IOException
import java.net.UnknownHostException // Import UnknownHostException


// SearchUiState remains the same for this solution, we'll make errorMessage more descriptive
data class SearchUiState(
    val ingredients: List<IngredientDTO> = emptyList(),
    val recipes: List<RecipeSummaryIM> = emptyList(),
    val isLoadingIngredients: Boolean = false,
    val isLoadingRecipes: Boolean = false,
    val selectedIngredient: String? = null,
    val errorMessage: String? = null,
    val noRecipesMessage: String? = null
)

class SearchViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var lastFetchTimeMillis: Long = 0
    private val fetchDebounceMillis: Long = 5000 // 5 seconds

    init { fetchIngredients() }

    fun fetchIngredients() {
        viewModelScope.launch {
            // Clear previous errors when starting a new fetch
            _uiState.value = _uiState.value.copy(isLoadingIngredients = true, errorMessage = null, noRecipesMessage = null)
            try {
                val ingredientsList = repository.getAllIngredients()
                _uiState.value = _uiState.value.copy(
                    ingredients = ingredientsList ?: emptyList(),
                    isLoadingIngredients = false
                )
                if (ingredientsList == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Could not load ingredients. Check connection and swipe to retry.")
                }
            } catch (e: Exception) {
                val specificMessage = if (e is UnknownHostException || e is IOException) {
                    "Connection lost. Could not load ingredients."
                } else {
                    "Error fetching ingredients: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoadingIngredients = false,
                    errorMessage = specificMessage
                )
            }
        }
    }

    fun fetchRecipesByIngredient(ingredientName: String, forceRefresh: Boolean = false) {
        val currentTime = SystemClock.elapsedRealtime()
        if (!forceRefresh && (currentTime - lastFetchTimeMillis < fetchDebounceMillis) && ingredientName == _uiState.value.selectedIngredient) {
            _uiState.value = _uiState.value.copy(isLoadingRecipes = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingRecipes = true,
                selectedIngredient = ingredientName,
                recipes = emptyList(),      // Clear previous recipes
                errorMessage = null,        // Clear previous errors for this specific fetch
                noRecipesMessage = null     // Clear previous no recipes message
            )
            try {
                val recipesList = repository.getRecipesByIngredient(ingredientName)

                if (recipesList != null) { // API call was successful in some form (might be empty list)
                    _uiState.value = _uiState.value.copy(
                        recipes = recipesList,
                        isLoadingRecipes = false,
                        noRecipesMessage = if (recipesList.isEmpty()) "Sorry, no recipes for '$ingredientName' exist." else null,
                        errorMessage = null // Clear error on successful fetch
                    )
                } else {
                    // This case implies the repository returned null, which it does on an exception during fetch.
                    // The catch block below should ideally handle this.
                    // However, to be safe, if recipesList is null and not caught as specific exception:
                    _uiState.value = _uiState.value.copy(
                        isLoadingRecipes = false,
                        errorMessage = "Could not load recipes for '$ingredientName'. Please check your connection.",
                        recipes = emptyList()
                    )
                }
                lastFetchTimeMillis = SystemClock.elapsedRealtime()
            } catch (e: Exception) {
                // This is where we explicitly check for network errors
                val specificMessage = if (e is UnknownHostException || e is IOException) {
                    "Connection lost. Please check your internet connection and try again."
                } else {
                    "An error occurred while fetching recipes for '$ingredientName'."
                }
                _uiState.value = _uiState.value.copy(
                    isLoadingRecipes = false,
                    errorMessage = specificMessage,
                    recipes = emptyList(),
                    noRecipesMessage = null // Ensure noRecipesMessage is cleared if there's an error
                )
            }
        }
    }

    fun errorMessageShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun prepopulateDatabase() {
        viewModelScope.launch {
            repository.prepopulateDatabaseWithRecipes()
        }
    }
}
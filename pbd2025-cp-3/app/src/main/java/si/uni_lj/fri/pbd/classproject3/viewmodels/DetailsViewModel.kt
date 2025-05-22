package si.uni_lj.fri.pbd.classproject3.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import si.uni_lj.fri.pbd.classproject3.models.RecipeDetailsIM
import si.uni_lj.fri.pbd.classproject3.repository.RecipeRepository

data class DetailsUiState(
    val recipeDetails: RecipeDetailsIM? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null
)

class DetailsViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    /**
     * Fetches recipe details.
     * @param recipeId The ID of the recipe to fetch.
     * @param startedFromSearchScreen True if navigation is from SearchScreen (fetch from API),
     * false if from FavoritesScreen (fetch from DB).
     */
    fun fetchRecipeDetails(recipeId: String, startedFromSearchScreen: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val details = if (startedFromSearchScreen) {
                    repository.getRecipeDetailsFromApi(recipeId)
                } else {
                    repository.getRecipeDetailsFromDb(recipeId)
                }

                if (details != null) {
                    _uiState.value = _uiState.value.copy(
                        recipeDetails = details,
                        isLoading = false,
                        isFavorite = details.isFavorite ?: repository.isFavorite(details.idMeal) // Ensure favorite status is up-to-date
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Could not load recipe details."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error fetching recipe details: ${e.message}"
                )
            }
        }
    }

    /**
     * Toggles the favorite status of the current recipe.
     */
    fun toggleFavorite() {
        val currentRecipe = _uiState.value.recipeDetails
        if (currentRecipe?.idMeal == null) return

        viewModelScope.launch {
            // Optimistically update UI, then call repository
            val newFavoriteState = !_uiState.value.isFavorite
            _uiState.value = _uiState.value.copy(isFavorite = newFavoriteState)

            // Create a new RecipeDetailsIM with the updated favorite status for the repository
            val updatedRecipeForRepo = currentRecipe.copy(isFavorite = newFavoriteState)

            try {
                repository.toggleFavoriteStatus(updatedRecipeForRepo)

                // Re-check from repository to be absolutely sure after toggle.
                val freshFavoriteStatus = repository.isFavorite(currentRecipe.idMeal)
                _uiState.value = _uiState.value.copy(isFavorite = freshFavoriteStatus, recipeDetails = currentRecipe.copy(isFavorite = freshFavoriteStatus))

            } catch (e: Exception) {
                // Revert optimistic update on error
                _uiState.value = _uiState.value.copy(
                    isFavorite = !newFavoriteState, // Revert
                    errorMessage = "Could not update favorite status: ${e.message}"
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
}

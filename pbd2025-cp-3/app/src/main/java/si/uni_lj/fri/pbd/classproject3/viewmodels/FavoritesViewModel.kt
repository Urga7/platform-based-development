package si.uni_lj.fri.pbd.classproject3.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import si.uni_lj.fri.pbd.classproject3.models.RecipeSummaryIM
import si.uni_lj.fri.pbd.classproject3.repository.RecipeRepository

data class FavoritesUiState(
    val favoriteRecipes: List<RecipeSummaryIM> = emptyList(),
    val isLoading: Boolean = true, // Initially true until the first emission from the flow
    val errorMessage: String? = null
)

class FavoritesViewModel(repository: RecipeRepository) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = repository.getFavoriteRecipes()
        .map { recipes -> FavoritesUiState(favoriteRecipes = recipes, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep flow active for 5s after last subscriber
            initialValue = FavoritesUiState(isLoading = true) // Initial state while waiting for first data
        )
}
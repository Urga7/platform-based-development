package si.uni_lj.fri.pbd.classproject3.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import si.uni_lj.fri.pbd.classproject3.database.RecipeDatabase
import si.uni_lj.fri.pbd.classproject3.repository.RecipeRepository
import si.uni_lj.fri.pbd.classproject3.rest.ServiceGenerator
import si.uni_lj.fri.pbd.classproject3.rest.RestAPI

/**
 * Factory for creating ViewModels with dependencies.
 *
 * @param application The application context, needed to initialize the database and repository.
 */
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    // Lazily initialized repository. It's created once and reused.
    private val recipeRepository: RecipeRepository by lazy {
        val db = RecipeDatabase.getDatabase(application)
        val api = ServiceGenerator.createService(RestAPI::class.java)
        RecipeRepository(db.recipeDao(), api)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(recipeRepository) as T
        }
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(recipeRepository) as T
        }
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailsViewModel(recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
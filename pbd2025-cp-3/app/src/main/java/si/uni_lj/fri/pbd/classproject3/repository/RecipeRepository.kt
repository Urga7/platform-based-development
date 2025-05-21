package si.uni_lj.fri.pbd.classproject3.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import si.uni_lj.fri.pbd.classproject3.database.dao.RecipeDao
import si.uni_lj.fri.pbd.classproject3.models.Mapper
import si.uni_lj.fri.pbd.classproject3.models.RecipeDetailsIM
import si.uni_lj.fri.pbd.classproject3.models.RecipeSummaryIM
import si.uni_lj.fri.pbd.classproject3.models.dto.IngredientDTO
import si.uni_lj.fri.pbd.classproject3.rest.RestAPI

/**
 * Repository class for handling data operations.
 * This class abstracts the data sources (network and local database) from the ViewModels.
 *
 * @property recipeDao The Data Access Object for recipe data.
 * @property restApi The Retrofit API service for fetching data from the network.
 */
class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val restApi: RestAPI
) {

    companion object {
        private const val TAG = "RecipeRepository"
    }

    /**
     * Fetches a list of all possible main ingredients from the remote API.
     *
     * @return A list of [IngredientDTO] objects or null if an error occurs.
     */
    suspend fun getAllIngredients(): List<IngredientDTO>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = restApi.getAllIngredients()
                response?.ingredients
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching ingredients: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Fetches a list of recipe summaries for a given main ingredient from the remote API.
     *
     * @param ingredient The main ingredient to filter recipes by.
     * @return A list of [si.uni_lj.fri.pbd.classproject3.models.RecipeSummaryIM] objects or null if an error occurs or no recipes are found.
     */
    suspend fun getRecipesByIngredient(ingredient: String): List<RecipeSummaryIM>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = restApi.getRecipesByIngredient(ingredient)
                response?.recipes?.map { Mapper.mapRecipeSummaryDtoToRecipeSummaryIm(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipes by ingredient '$ingredient': ${e.message}", e)
                null
            }
        }
    }

    /**
     * Fetches the details for a specific recipe by its ID from the remote API.
     * It also checks if the recipe is marked as a favorite in the local database.
     *
     * @param recipeId The ID of the recipe.
     * @return A [si.uni_lj.fri.pbd.classproject3.models.RecipeDetailsIM] object or null if an error occurs or the recipe is not found.
     */
    suspend fun getRecipeDetailsFromApi(recipeId: String): RecipeDetailsIM? {
        return withContext(Dispatchers.IO) {
            try {
                val response = restApi.getRecipeDetailsById(recipeId)
                val recipeDetailsDto = response?.recipes?.firstOrNull()
                if (recipeDetailsDto != null) {
                    // Check if this recipe is already a favorite to set the initial state
                    val localRecipe = recipeDao.getRecipeByMealId(recipeId)
                    Mapper.mapRecipeDetailsDtoToRecipeDetailsIm(
                        localRecipe?.isFavorite,
                        recipeDetailsDto
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error fetching recipe details for ID '$recipeId' from API: ${e.message}",
                    e
                )
                null
            }
        }
    }

    /**
     * Fetches the details for a specific recipe by its ID from the local database.
     *
     * @param recipeId The ID of the recipe.
     * @return A [RecipeDetailsIM] object or null if the recipe is not found in the database.
     */
    suspend fun getRecipeDetailsFromDb(recipeId: String): RecipeDetailsIM? {
        return withContext(Dispatchers.IO) {
            try {
                val recipeDetailsEntity = recipeDao.getRecipeByMealId(recipeId)
                recipeDetailsEntity?.let {
                    Mapper.mapRecipeDetailsToRecipeDetailsIm(it.isFavorite, it)
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error fetching recipe details for ID '$recipeId' from DB: ${e.message}",
                    e
                )
                null
            }
        }
    }

    /**
     * Retrieves a flow of all favorite recipes from the local database.
     * The recipes are mapped to [RecipeSummaryIM] for display in lists.
     *
     * @return A Flow emitting a list of [RecipeSummaryIM] objects.
     */
    fun getFavoriteRecipes(): Flow<List<RecipeSummaryIM>> {
        // The flow from Room is already asynchronous and will run on a background thread.
        // Mapping is done on the collector's context, typically Dispatchers.Main for UI.
        return recipeDao.getFavoriteRecipes().map { list ->
            list.map { Mapper.mapRecipeDetailsToRecipeSummaryIm(it) }
        }
    }

    /**
     * Adds or updates a recipe in the local database, marking it as a favorite.
     * If the recipe details are not fully present, it tries to fetch them from the API.
     *
     * @param recipe The [RecipeDetailsIM] object to add/update as a favorite.
     */
    suspend fun addRecipeToFavorites(recipe: RecipeDetailsIM) {
        withContext(Dispatchers.IO) {
            try {
                var fullRecipe = recipe
                // If instructions are missing, it might be a summary. Fetch full details.
                if (fullRecipe.strInstructions.isNullOrEmpty() && fullRecipe.idMeal != null) {
                    val fetchedDetails = getRecipeDetailsFromApi(fullRecipe.idMeal!!)
                    if (fetchedDetails != null) {
                        fullRecipe = fetchedDetails
                    } else {
                        Log.w(
                            TAG,
                            "Could not fetch full details for recipe ${fullRecipe.idMeal} before favoriting."
                        )
                        // Proceed with potentially incomplete data if API fetch fails
                    }
                }

                val recipeEntity = Mapper.mapRecipeDetailsImToRecipeDetails(true, fullRecipe)
                recipeEntity.isFavorite = true // Ensure it's marked as favorite
                recipeDao.insertRecipe(recipeEntity)
                Log.d(TAG, "Recipe ${recipeEntity.idMeal} added to favorites.")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding recipe ${recipe.idMeal} to favorites: ${e.message}", e)
            }
        }
    }

    /**
     * Updates a recipe in the local database, typically to mark it as not a favorite.
     *
     * @param recipe The [RecipeDetailsIM] object to update (e.g., to unfavorite).
     */
    suspend fun removeRecipeFromFavorites(recipe: RecipeDetailsIM) {
        withContext(Dispatchers.IO) {
            try {
                val recipeEntity = Mapper.mapRecipeDetailsImToRecipeDetails(false, recipe)
                recipeEntity.isFavorite = false // Ensure it's marked as not favorite
                // We use insert because OnConflictStrategy.REPLACE will update it.
                // Alternatively, you could fetch by idMeal, update the entity, then call recipeDao.updateRecipe().
                recipeDao.insertRecipe(recipeEntity)
                Log.d(TAG, "Recipe ${recipeEntity.idMeal} removed from favorites.")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing recipe ${recipe.idMeal} from favorites: ${e.message}", e)
            }
        }
    }

    /**
     * Checks if a recipe is a favorite.
     * @param recipeId The ID of the recipe.
     * @return True if the recipe is a favorite, false otherwise.
     */
    suspend fun isFavorite(recipeId: String?): Boolean {
        if (recipeId == null) return false
        return withContext(Dispatchers.IO) {
            recipeDao.getRecipeByMealId(recipeId)?.isFavorite ?: false
        }
    }

    /**
     * Toggles the favorite status of a recipe.
     * If it's a favorite, it's removed. If not, it's added.
     *
     * @param recipeDetailsIM The recipe whose favorite status is to be toggled.
     */
    suspend fun toggleFavoriteStatus(recipeDetailsIM: RecipeDetailsIM) {
        withContext(Dispatchers.IO) {
            if (recipeDetailsIM.idMeal == null) {
                Log.e(TAG, "Cannot toggle favorite status for recipe with null idMeal.")
                return@withContext
            }
            val isCurrentlyFavorite = isFavorite(recipeDetailsIM.idMeal)
            if (isCurrentlyFavorite) {
                removeRecipeFromFavorites(recipeDetailsIM)
            } else {
                addRecipeToFavorites(recipeDetailsIM)
            }
        }
    }

    /**
     * Fetches a few random recipes from the API and stores them in the local database.
     * This is intended for pre-populating the database during SplashScreen.
     * It fetches a list of common ingredients first, then picks a few to get recipes.
     *
     * @param numberOfRecipesToPrepopulate Approximate number of recipes to try and fetch.
     */
    suspend fun prepopulateDatabaseWithRandomRecipes(numberOfRecipesToPrepopulate: Int = 5) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting database pre-population...")
                // A few common ingredients to try
                val commonIngredients = listOf(
                    "Chicken",
                    "Beef",
                    "Salmon",
                    "Eggs",
                    "Pasta",
                    "Potatoes",
                    "Onion",
                    "Garlic",
                    "Tomatoes",
                    "Cheese"
                )
                var recipesAdded = 0

                for (ingredientName in commonIngredients.shuffled()) {
                    if (recipesAdded >= numberOfRecipesToPrepopulate) break

                    val recipesByIngredient = restApi.getRecipesByIngredient(ingredientName)
                    recipesByIngredient?.recipes?.take(2)?.forEach { recipeSummaryDto ->
                        if (recipesAdded >= numberOfRecipesToPrepopulate) return@forEach
                        if (recipeSummaryDto.id == null) return@forEach

                        // Check if already in DB (not necessarily as favorite)
                        val existingRecipe = recipeDao.getRecipeByMealId(recipeSummaryDto.id)
                        if (existingRecipe == null) {
                            val recipeDetailsDto =
                                restApi.getRecipeDetailsById(recipeSummaryDto.id)?.recipes?.firstOrNull()
                            if (recipeDetailsDto != null) {
                                val recipeEntity = Mapper.mapRecipeDetailsDtoToRecipeDetails(
                                    false,
                                    recipeDetailsDto
                                ) // Not favorite by default
                                recipeDao.insertRecipe(recipeEntity)
                                recipesAdded++
                                Log.d(TAG, "Pre-populated with: ${recipeEntity.strMeal}")
                            }
                        }
                    }
                }
                Log.d(TAG, "Database pre-population attempt finished. Added $recipesAdded recipes.")
            } catch (e: Exception) {
                Log.e(TAG, "Error during database pre-population: ${e.message}", e)
            }
        }
    }
}
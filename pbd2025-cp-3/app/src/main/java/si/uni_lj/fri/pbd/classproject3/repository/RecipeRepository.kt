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
class RecipeRepository(private val recipeDao: RecipeDao, private val restApi: RestAPI) {

    companion object { private const val TAG = "RecipeRepository" }

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
                val dtoList = response?.recipes
                if (dtoList == null && response != null) {
                    emptyList<RecipeSummaryIM>()
                } else {
                    dtoList?.map { Mapper.mapRecipeSummaryDtoToRecipeSummaryIm(it) }
                }
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
                    val localRecipe = recipeDao.getRecipeByMealId(recipeId)
                    Mapper.mapRecipeDetailsDtoToRecipeDetailsIm(localRecipe?.isFavorite, recipeDetailsDto)
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recipe details for ID '$recipeId' from API: ${e.message}", e)
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
                Log.e(TAG, "Error fetching recipe details for ID '$recipeId' from DB: ${e.message}", e)
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
        return recipeDao.getFavoriteRecipes().map { list ->
            list.map { Mapper.mapRecipeDetailsToRecipeSummaryIm(it) }
        }
    }

    /**
     * Adds or updates a recipe in the local database, marking it as a favorite.
     * If the recipe details are not fully present, it tries to fetch them from the API.
     *
     * @param recipeIM The [RecipeDetailsIM] object to add/update as a favorite.
     */
    suspend fun addRecipeToFavorites(recipeIM: RecipeDetailsIM) {
        withContext(Dispatchers.IO) {
            if (recipeIM.idMeal == null) {
                Log.e(TAG, "Cannot add favorite, idMeal is null")
                return@withContext
            }
            try {
                var recipeToAdd = recipeIM
                // If instructions are missing, it might be a summary from search. Fetch full details.
                if (recipeToAdd.strInstructions.isNullOrEmpty()) {
                    val fetchedDetails = getRecipeDetailsFromApi(recipeToAdd.idMeal!!)
                    if (fetchedDetails != null) {
                        recipeToAdd = fetchedDetails // Use full details
                    } else {
                        Log.w(TAG, "Could not fetch full details for recipe ${recipeToAdd.idMeal} before favoriting. Proceeding with current details.")
                    }
                }

                val existingEntity = recipeDao.getRecipeByMealId(recipeToAdd.idMeal!!)
                if (existingEntity != null) {
                    // Recipe exists, update its favorite status
                    existingEntity.isFavorite = true
                    // Update all other fields from recipeToAdd in case they were fetched fresh
                    val updatedEntity = Mapper.mapRecipeDetailsImToRecipeDetails(true, recipeToAdd)
                    updatedEntity.id = existingEntity.id // CRITICAL: Preserve existing database ID for update
                    recipeDao.updateRecipe(updatedEntity)
                    Log.d(TAG, "Recipe ${existingEntity.idMeal} updated to favorite.")
                } else {
                    // Recipe does not exist, insert new
                    val newEntity = Mapper.mapRecipeDetailsImToRecipeDetails(true, recipeToAdd)
                    newEntity.isFavorite = true // Ensure it's marked
                    recipeDao.insertRecipe(newEntity)
                    Log.d(TAG, "Recipe ${newEntity.idMeal} added to favorites as new entry.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding recipe ${recipeIM.idMeal} to favorites: ${e.message}", e)
            }
        }
    }

    /**
     * Updates a recipe in the local database, typically to mark it as not a favorite.
     *
     * @param recipeIM The [RecipeDetailsIM] object to update (e.g., to unfavorite).
     */
    suspend fun removeRecipeFromFavorites(recipeIM: RecipeDetailsIM) {
        withContext(Dispatchers.IO) {
            if (recipeIM.idMeal == null) {
                Log.e(TAG, "Cannot remove favorite, idMeal is null")
                return@withContext
            }
            try {
                val existingEntity = recipeDao.getRecipeByMealId(recipeIM.idMeal!!)
                if (existingEntity == null) {
                    // Should not happen if we are trying to unfavorite, implies it was never in DB
                    Log.w(TAG, "Recipe ${recipeIM.idMeal} not found in DB to remove from favorites.")
                    return@withContext
                }

                // Recipe exists, update its favorite status
                existingEntity.isFavorite = false

                // We can just update the flag on the existing entity
                recipeDao.updateRecipe(existingEntity)
                Log.d(TAG, "Recipe ${existingEntity.idMeal} removed from favorites (updated to not favorite).")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing recipe ${recipeIM.idMeal} from favorites: ${e.message}", e)
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
            recipeDao.getRecipeByMealId(recipeId)?.isFavorite == true
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

            if (recipeDetailsIM.isFavorite == true) {
                addRecipeToFavorites(recipeDetailsIM)
            } else {
                removeRecipeFromFavorites(recipeDetailsIM)
            }
        }
    }

    /**
     * Fetches the main ingredient list from the API. Then, for a specified number of these
     * ingredients, it fetches recipes and stores them in the local database
     * if they don't already exist.
     *
     * @param numberOfIngredientsToProcess The number of ingredients from the fetched list
     * for which recipes should be fetched and pre-populated.
     */
    suspend fun prepopulateDatabaseWithRecipes(numberOfIngredientsToProcess: Int = 4) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Pre-population step: Fetching main ingredient list from API...")
                val allIngredientsResponse = restApi.getAllIngredients() // Fetch the ingredient list
                val availableIngredients = allIngredientsResponse?.ingredients

                if (availableIngredients.isNullOrEmpty()) {
                    Log.w(TAG, "Failed to fetch main ingredient list or list is empty. Skipping recipe pre-population.")
                    return@withContext
                }
                Log.d(TAG, "Successfully fetched ${availableIngredients.size} ingredients from API.")

                if (numberOfIngredientsToProcess <= 0) {
                    Log.d(TAG, "Recipe pre-population skipped as numberOfIngredientsToProcess or recipesPerIngredient is 0.")
                    return@withContext
                }

                Log.d(TAG, "Starting database pre-population using recipes from the first $numberOfIngredientsToProcess fetched ingredients...")
                var recipesActuallyAdded = 0 // To count total recipes added
                val ingredientsToProcess = availableIngredients.take(numberOfIngredientsToProcess)
                for (ingredientDto in ingredientsToProcess) {
                    val ingredientName = ingredientDto.strIngredient
                    if (ingredientName.isNullOrBlank()) continue

                    Log.d(TAG, "Processing ingredient for recipe pre-population: $ingredientName")
                    val recipesByIngredient = restApi.getRecipesByIngredient(ingredientName)
                    recipesByIngredient?.recipes?.forEach { recipeSummaryDto ->
                        if (recipeSummaryDto.id == null) return@forEach

                        val existingRecipe = recipeDao.getRecipeByMealId(recipeSummaryDto.id)
                        if(existingRecipe != null) return@forEach

                        val recipeDetailsDto = restApi.getRecipeDetailsById(recipeSummaryDto.id)?.recipes?.firstOrNull()
                        if (recipeDetailsDto == null) return@forEach

                        val recipeEntity = Mapper.mapRecipeDetailsDtoToRecipeDetails(false, recipeDetailsDto)
                        recipeDao.insertRecipe(recipeEntity)
                        recipesActuallyAdded++
                        Log.d(TAG, "Pre-populated DB with recipe: ${recipeEntity.strMeal} (from ingredient: $ingredientName)")
                    }
                }
                Log.d(TAG, "Database recipe pre-population attempt finished. Added $recipesActuallyAdded recipes from $numberOfIngredientsToProcess processed ingredients.")
            } catch (e: Exception) {
                Log.e(TAG, "Error during pre-population tasks: ${e.message}", e)
            }
        }
    }
}
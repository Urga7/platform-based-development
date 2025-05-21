package si.uni_lj.fri.pbd.classproject3.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import si.uni_lj.fri.pbd.classproject3.database.entity.RecipeDetails

@Dao
interface RecipeDao {
    /**
     * Retrieves a specific recipe by its meal ID.
     * @param idMeal The ID of the meal.
     * @return The RecipeDetails object if found, otherwise null.
     */
    @Query("SELECT * FROM RecipeDetails WHERE idMeal = :idMeal")
    suspend fun getRecipeByMealId(idMeal: String?): RecipeDetails?

    /**
     * Inserts a recipe into the database. If the recipe already exists, it replaces it.
     * @param recipe The recipe to be inserted.
     * @return The row ID of the newly inserted recipe.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeDetails): Long

    /**
     * Updates an existing recipe in the database.
     * @param recipe The recipe with updated fields.
     */
    @Update
    suspend fun updateRecipe(recipe: RecipeDetails)

    /**
     * Retrieves all recipes marked as favorites, ordered by their name.
     * Returns a Flow for reactive updates.
     * @return A Flow emitting a list of favorite RecipeDetails.
     */
    @Query("SELECT * FROM RecipeDetails WHERE isFavorite = 1 ORDER BY strMeal ASC")
    fun getFavoriteRecipes(): Flow<List<RecipeDetails>>

    /**
     * Deletes a recipe by its meal ID.
     * This might be useful if a recipe is unfavorited and not available from the server.
     * @param idMeal The ID of the meal to delete.
     */
    @Query("DELETE FROM RecipeDetails WHERE idMeal = :idMeal")
    suspend fun deleteRecipeByMealId(idMeal: String?)

    /**
     * Retrieves all recipes currently stored in the database.
     * Useful for pre-populating or debugging.
     * @return A list of all RecipeDetails.
     */
    @Query("SELECT * FROM RecipeDetails")
    suspend fun getAllRecipes(): List<RecipeDetails>
}
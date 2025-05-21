package si.uni_lj.fri.pbd.classproject3.rest

import retrofit2.http.GET
import retrofit2.http.Query
import si.uni_lj.fri.pbd.classproject3.models.dto.IngredientsDTO
import si.uni_lj.fri.pbd.classproject3.models.dto.RecipesByIngredientDTO
import si.uni_lj.fri.pbd.classproject3.models.dto.RecipesByIdDTO

interface RestAPI {

    @GET("list.php?i=list")
    suspend fun getAllIngredients(): IngredientsDTO?

    /**
     * Fetches all recipes that contain a certain main ingredient.
     * Endpoint: filter.php
     * @param ingredient The main ingredient to filter by (e.g., "chicken_breast").
     * @return A DTO containing a list of recipe summaries.
     */
    @GET("filter.php")
    suspend fun getRecipesByIngredient(@Query("i") ingredient: String): RecipesByIngredientDTO?

    /**
     * Fetches recipe details for a specific recipe ID.
     * Endpoint: lookup.php
     * @param recipeId The ID of the recipe to look up.
     * @return A DTO containing the details of the recipe (usually a list with one item).
     */
    @GET("lookup.php")
    suspend fun getRecipeDetailsById(@Query("i") recipeId: String): RecipesByIdDTO?
}
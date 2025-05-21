package si.uni_lj.fri.pbd.classproject3.models.dto

import com.google.gson.annotations.SerializedName

data class RecipesByIdDTO(
    @SerializedName("meals")
    val recipes: List<RecipeDetailsDTO>? // API returns a list, but it should contain one item or be null
)
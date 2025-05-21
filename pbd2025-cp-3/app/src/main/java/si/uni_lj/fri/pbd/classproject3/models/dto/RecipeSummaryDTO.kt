package si.uni_lj.fri.pbd.classproject3.models.dto

import com.google.gson.annotations.SerializedName

data class RecipeSummaryDTO(
    @SerializedName("idMeal")
    val id: String?,

    @SerializedName("strMeal")
    val name: String?,

    @SerializedName("strMealThumb")
    val thumbnailUrl: String?
)

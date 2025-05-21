package si.uni_lj.fri.pbd.classproject3.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class IngredientsDTO {
    @SerializedName("meals")
    @Expose
    val ingredients: List<IngredientDTO>? = null
}
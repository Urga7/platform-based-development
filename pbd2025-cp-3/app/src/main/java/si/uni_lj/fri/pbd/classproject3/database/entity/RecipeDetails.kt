package si.uni_lj.fri.pbd.classproject3.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class RecipeDetails(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean? = null,

    @ColumnInfo(name = "idMeal")
    var idMeal: String? = null,

    @ColumnInfo(name = "strMeal")
    var strMeal: String? = null,

    @ColumnInfo(name = "strCategory")
    var strCategory: String? = null,

    @ColumnInfo(name = "strArea")
    var strArea: String? = null,

    @ColumnInfo(name = "strInstructions")
    var strInstructions: String? = null,

    @ColumnInfo(name = "strMealThumb")
    var strMealThumb: String? = null,

    @ColumnInfo(name = "strYoutube")
    var strYoutube: String? = null,

    @ColumnInfo(name = "strIngredient1")
    var strIngredient1: String? = null,

    @ColumnInfo(name = "strIngredient2")
    var strIngredient2: String? = null,

    @ColumnInfo(name = "strIngredient3")
    var strIngredient3: String? = null,

    @ColumnInfo(name = "strIngredient4")
    var strIngredient4: String? = null,

    @ColumnInfo(name = "strIngredient5")
    var strIngredient5: String? = null,

    @ColumnInfo(name = "strIngredient6")
    var strIngredient6: String? = null,

    @ColumnInfo(name = "strIngredient7")
    var strIngredient7: String? = null,

    @ColumnInfo(name = "strIngredient8")
    var strIngredient8: String? = null,

    @ColumnInfo(name = "strIngredient9")
    var strIngredient9: String? = null,

    @ColumnInfo(name = "strIngredient10")
    var strIngredient10: String? = null,

    @ColumnInfo(name = "strIngredient11")
    var strIngredient11: String? = null,

    @ColumnInfo(name = "strIngredient12")
    var strIngredient12: String? = null,

    @ColumnInfo(name = "strIngredient13")
    var strIngredient13: String? = null,

    @ColumnInfo(name = "strIngredient14")
    var strIngredient14: String? = null,

    @ColumnInfo(name = "strIngredient15")
    var strIngredient15: String? = null,

    @ColumnInfo(name = "strIngredient16")
    var strIngredient16: String? = null,

    @ColumnInfo(name = "strIngredient17")
    var strIngredient17: String? = null,

    @ColumnInfo(name = "strIngredient18")
    var strIngredient18: String? = null,

    @ColumnInfo(name = "strIngredient19")
    var strIngredient19: String? = null,

    @ColumnInfo(name = "strIngredient20")
    var strIngredient20: String? = null,

    @ColumnInfo(name = "strMeasure1")
    var strMeasure1: String? = null,

    @ColumnInfo(name = "strMeasure2")
    var strMeasure2: String? = null,

    @ColumnInfo(name = "strMeasure3")
    var strMeasure3: String? = null,

    @ColumnInfo(name = "strMeasure4")
    var strMeasure4: String? = null,

    @ColumnInfo(name = "strMeasure5")
    var strMeasure5: String? = null,

    @ColumnInfo(name = "strMeasure6")
    var strMeasure6: String? = null,

    @ColumnInfo(name = "strMeasure7")
    var strMeasure7: String? = null,

    @ColumnInfo(name = "strMeasure8")
    var strMeasure8: String? = null,

    @ColumnInfo(name = "strMeasure9")
    var strMeasure9: String? = null,

    @ColumnInfo(name = "strMeasure10")
    var strMeasure10: String? = null,

    @ColumnInfo(name = "strMeasure11")
    var strMeasure11: String? = null,

    @ColumnInfo(name = "strMeasure12")
    var strMeasure12: String? = null,

    @ColumnInfo(name = "strMeasure13")
    var strMeasure13: String? = null,

    @ColumnInfo(name = "strMeasure14")
    var strMeasure14: String? = null,

    @ColumnInfo(name = "strMeasure15")
    var strMeasure15: String? = null,

    @ColumnInfo(name = "strMeasure16")
    var strMeasure16: String? = null,

    @ColumnInfo(name = "strMeasure17")
    var strMeasure17: String? = null,

    @ColumnInfo(name = "strMeasure18")
    var strMeasure18: String? = null,

    @ColumnInfo(name = "strMeasure19")
    var strMeasure19: String? = null,

    @ColumnInfo(name = "strMeasure20")
    var strMeasure20: String? = null,

    @ColumnInfo(name = "strSource")
    var strSource: String? = null
) {
    @Ignore
    constructor() : this(null)
}

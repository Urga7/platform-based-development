package si.uni_lj.fri.pbd.classproject3.models

data class RecipeSummaryIM(
    val strMeal: String,
    val strMealThumb: String,
    val idMeal: String
) {
    override fun toString(): String {
        return "RecipeSummaryIM(strMeal='$strMeal', strMealThumb='$strMealThumb', idMeal='$idMeal')"
    }
}


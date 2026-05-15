package dam.a51394.nevazio.data.model

data class Recipe(
    val id: String,
    val name: String,
    val timeMinutes: Int,
    val difficulty: String,
    val tags: List<String>,
    val matchPercentage: Int,
    val missingIngredients: Int
)

data class RecipeStep(
    val number: Int,
    val description: String
)

data class RecipeIngredient(
    val ingredient: Ingredient,
    val isAvailable: Boolean
)

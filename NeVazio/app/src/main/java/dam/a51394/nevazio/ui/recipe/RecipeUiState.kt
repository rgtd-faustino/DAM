package dam.a51394.nevazio.ui.recipe

import dam.a51394.nevazio.data.model.Ingredient

data class RecipeUiState(
    val title: String = "",
    val imageUrl: String = "",
    val time: String = "",
    val difficulty: String = "",
    val isVegetarian: Boolean = false,
    val tags: List<String> = emptyList(),
    val ingredients: List<Pair<Ingredient, Boolean>> = emptyList(),
    val steps: List<String> = emptyList(),
    val isLoading: Boolean = false
)

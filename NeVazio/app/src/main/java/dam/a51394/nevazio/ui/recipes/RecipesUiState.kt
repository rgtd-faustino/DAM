package dam.a51394.nevazio.ui.recipes

import dam.a51394.nevazio.data.model.Recipe

data class RecipesUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

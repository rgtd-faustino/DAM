package dam.a51394.nevazio.ui.recipes

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecipesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        val mockRecipes = listOf(
            Recipe(
                id = "1",
                name = "Ovos mexidos com queijo",
                timeMinutes = 15,
                difficulty = "Fácil",
                tags = listOf("Fácil", "Vegetariano"),
                matchPercentage = 95,
                missingIngredients = 0
            ),
            Recipe(
                id = "2",
                name = "Sopa de brócolos",
                timeMinutes = 30,
                difficulty = "Fácil",
                tags = listOf("Saudável"),
                matchPercentage = 80,
                missingIngredients = 2
            ),
            Recipe(
                id = "3",
                name = "Omelete de queijo",
                timeMinutes = 10,
                difficulty = "Fácil",
                tags = listOf("Fácil"),
                matchPercentage = 70,
                missingIngredients = 3
            )
        )
        _uiState.update { it.copy(recipes = mockRecipes) }
    }
}

package dam.a51394.nevazio.ui.recipes

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import androidx.lifecycle.viewModelScope
import dam.a51394.nevazio.data.repository.AuthRepository
import dam.a51394.nevazio.data.repository.FridgeRepository
import dam.a51394.nevazio.data.repository.RecipeRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecipesViewModel(
    private val authRepository: AuthRepository,
    private val fridgeRepository: FridgeRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        val user = authRepository.currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentFridgeId = authRepository.getCurrentFridgeId()

            fridgeRepository.getIngredients(currentFridgeId).collectLatest { ingredients ->
                val ingredientNames = ingredients.map { it.name }
                
                if (ingredientNames.isEmpty()) {
                    _uiState.update { it.copy(recipes = emptyList(), isLoading = false, errorMessage = null) }
                } else {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    try {
                        val apiRecipes = recipeRepository.fetchRecipesForIngredients(ingredientNames)
                        if (apiRecipes.isEmpty()) {
                            _uiState.update { it.copy(recipes = emptyList(), isLoading = false, errorMessage = "Nenhuma receita encontrada. (Talvez o limite diário da API Spoonacular tenha sido atingido?)") }
                        } else {
                            _uiState.update { it.copy(recipes = apiRecipes, isLoading = false, errorMessage = null) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(recipes = emptyList(), isLoading = false, errorMessage = "Erro na API: ${e.message}") }
                    }
                }
            }
        }
    }
}

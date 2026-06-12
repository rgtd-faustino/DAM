package dam.a51394.nevazio.ui.recipe

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import dam.a51394.nevazio.data.repository.RecipeRepository
import dam.a51394.nevazio.data.repository.FridgeRepository
import dam.a51394.nevazio.data.repository.AuthRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

import kotlinx.coroutines.tasks.await

class RecipeViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository,
    private val fridgeRepository: FridgeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val state = repository.fetchRecipeDetails(recipeId)
            if (state != null) {
                // A utilização do authRepository.getCurrentFridgeId() centraliza a lógica de resolução
                // de frigorífico (Anfitrião vs Convidado), garantindo que a pesquisa de ingredientes da receita
                // é efetuada na base de dados correta. O código anterior assumia o código da família
                // como ID do frigorífico de forma direta, o que resultava na leitura de uma coleção fantasma
                // vazia, marcando incorretamente todos os ingredientes como em falta (X vermelho).
                val fridgeId = authRepository.getCurrentFridgeId()
                
                val fridgeIngredients = fridgeRepository.getIngredients(fridgeId).firstOrNull() ?: emptyList()
                val fridgeNamesEn = fridgeIngredients.map { 
                    dam.a51394.nevazio.data.repository.RecipeRepository.translatePtToEn(it.name) 
                }
                
                val updatedIngredients = state.ingredients.map { (ing, _) ->
                    val isAvailable = fridgeNamesEn.any { fNameEn ->
                        fNameEn.contains(ing.name.lowercase()) || ing.name.lowercase().contains(fNameEn)
                    }
                    ing to isAvailable
                }
                
                _uiState.value = state.copy(ingredients = updatedIngredients)
            }
        }
    }
}

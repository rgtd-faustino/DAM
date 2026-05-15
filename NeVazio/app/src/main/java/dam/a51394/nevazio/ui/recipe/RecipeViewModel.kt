package dam.a51394.nevazio.ui.recipe

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

class RecipeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val mockIngredients = listOf(
            Ingredient("1", "Ovos", "3 unidades", Date(), StorageLocation.FRIDGE, "egg", ExpiryStatus.FRESH, "20 Mai") to true,
            Ingredient("2", "Queijo Ralado", "50g", Date(), StorageLocation.FRIDGE, "cheese", ExpiryStatus.FRESH, "18 Mai") to true,
            Ingredient("3", "Manteiga", "1 colher", Date(), StorageLocation.FRIDGE, "water_drop", ExpiryStatus.EXPIRED, "") to false
        )

        val mockSteps = listOf(
            "Parta os ovos para uma tigela e bata ligeiramente com um garfo até misturar as gemas com as claras.",
            "Aqueça uma frigideira em lume médio e derreta a manteiga.",
            "Verta os ovos batidos na frigideira. Mexa suavemente e continuamente com uma espátula, trazendo as bordas cozidas para o centro.",
            "Quando os ovos estiverem quase no ponto desejado (mas ainda húmidos), adicione o queijo ralado e envolva bem. Retire do lume imediatamente."
        )

        _uiState.update {
            it.copy(
                title = "Ovos mexidos com queijo",
                time = "15 min",
                difficulty = "Fácil",
                isVegetarian = true,
                tags = listOf("Fácil", "Vegetariano"),
                ingredients = mockIngredients,
                steps = mockSteps
            )
        }
    }
}

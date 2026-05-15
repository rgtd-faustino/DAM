package dam.a51394.nevazio.ui.shopping

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShoppingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update {
            it.copy(
                itemsToBuy = listOf(
                    ShoppingItem("1", "Leite", "2L", "João"),
                    ShoppingItem("2", "Pão de Forma", "1un", "Maria"),
                    ShoppingItem("3", "Tomates", "1kg", "João"),
                    ShoppingItem("4", "Frango", "1kg", "Maria"),
                ),
                itemsBought = listOf(
                    ShoppingItem("5", "Ovos", "6un", "João", isBought = true),
                    ShoppingItem("6", "Azeite", "500ml", "Maria", isBought = true),
                )
            )
        }
    }

    fun toggleItem(item: ShoppingItem) {
        val currentToBuy = _uiState.value.itemsToBuy.toMutableList()
        val currentBought = _uiState.value.itemsBought.toMutableList()
        if (item.isBought) {
            currentBought.remove(item)
            currentToBuy.add(item.copy(isBought = false))
        } else {
            currentToBuy.remove(item)
            currentBought.add(item.copy(isBought = true))
        }
        _uiState.update { it.copy(itemsToBuy = currentToBuy, itemsBought = currentBought) }
    }
}

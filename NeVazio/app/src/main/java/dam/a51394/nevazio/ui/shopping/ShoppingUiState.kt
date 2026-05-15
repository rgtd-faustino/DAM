package dam.a51394.nevazio.ui.shopping

import dam.a51394.nevazio.data.model.ShoppingItem

data class ShoppingUiState(
    val itemsToBuy: List<ShoppingItem> = emptyList(),
    val itemsBought: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false
)

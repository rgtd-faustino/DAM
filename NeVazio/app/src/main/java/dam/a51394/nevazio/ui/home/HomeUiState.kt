package dam.a51394.nevazio.ui.home

import dam.a51394.nevazio.data.model.Ingredient

data class HomeUiState(
    val userName: String = "",
    val familyName: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val filteredIngredients: List<Ingredient> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0, // 0: Frigorífico, 1: Despensa, 2: Scan
    val showAddSheet: Boolean = false,
    val expiryWarning: String? = null
)

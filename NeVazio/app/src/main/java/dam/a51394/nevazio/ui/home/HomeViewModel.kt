package dam.a51394.nevazio.ui.home

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.ExpiryStatus
import dam.a51394.nevazio.data.model.Ingredient
import dam.a51394.nevazio.data.model.StorageLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val allIngredients = listOf(
        Ingredient("1", "Leite", "2L", Date(), StorageLocation.FRIDGE, "water_drop", ExpiryStatus.FRESH, "12 Mai"),
        Ingredient("2", "Brócolos", "500g", Date(), StorageLocation.FRIDGE, "eco", ExpiryStatus.EXPIRES_TODAY, "Hoje!"),
        Ingredient("3", "Iogurtes", "4un", Date(), StorageLocation.FRIDGE, "icecream", ExpiryStatus.EXPIRING_SOON, "2 dias"),
        Ingredient("4", "Ovos", "6un", Date(), StorageLocation.FRIDGE, "egg", ExpiryStatus.FRESH, "20 Mai"),
        Ingredient("5", "Queijo", "250g", Date(), StorageLocation.FRIDGE, "cheese", ExpiryStatus.FRESH, "18 Mai"),
        Ingredient("6", "Manteiga", "200g", Date(), StorageLocation.FRIDGE, "water_drop", ExpiryStatus.FRESH, "30 Mai"),
        Ingredient("7", "Farinha", "1kg", Date(), StorageLocation.PANTRY, "grain", ExpiryStatus.FRESH, "Dez 2025"),
        Ingredient("8", "Arroz", "2kg", Date(), StorageLocation.PANTRY, "grain", ExpiryStatus.FRESH, "Jan 2026"),
        Ingredient("9", "Azeite", "500ml", Date(), StorageLocation.PANTRY, "water_drop", ExpiryStatus.FRESH, "Mar 2026")
    )

    init {
        loadData()
    }

    private fun loadData() {
        val expiringToday = allIngredients.filter { it.status == ExpiryStatus.EXPIRES_TODAY }
        val warning = if (expiringToday.isNotEmpty()) {
            "⚠️ ${expiringToday.first().name} expira${if (expiringToday.size > 1) "m" else ""} hoje! Usa-${if (expiringToday.size > 1) "os" else "o"} já."
        } else null

        _uiState.update {
            it.copy(
                userName = "João",
                familyName = "Família Correia",
                ingredients = allIngredients,
                filteredIngredients = allIngredients.filter { ing -> ing.location == StorageLocation.FRIDGE },
                expiryWarning = warning,
                isLoading = false
            )
        }
    }

    fun onTabSelected(index: Int) {
        val location = if (index == 1) StorageLocation.PANTRY else StorageLocation.FRIDGE
        val filtered = if (index == 2) emptyList()
        else allIngredients.filter { it.location == location }
        _uiState.update { it.copy(selectedTab = index, filteredIngredients = filtered) }
    }

    fun onSearchQueryChange(query: String) {
        val state = _uiState.value
        val location = if (state.selectedTab == 1) StorageLocation.PANTRY else StorageLocation.FRIDGE
        val filtered = allIngredients
            .filter { it.location == location }
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) }
        _uiState.update { it.copy(searchQuery = query, filteredIngredients = filtered) }
    }

    fun showAddSheet() = _uiState.update { it.copy(showAddSheet = true) }
    fun hideAddSheet() = _uiState.update { it.copy(showAddSheet = false) }

    fun addIngredient(name: String, quantity: String, unit: String, location: StorageLocation) {
        if (name.isBlank()) return
        // In a real app, persist to database
        hideAddSheet()
    }
}

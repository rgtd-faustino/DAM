package dam.a51394.nevazio.ui.shopping

import androidx.lifecycle.ViewModel
import dam.a51394.nevazio.data.model.StorageLocation
import dam.a51394.nevazio.data.model.Ingredient
import dam.a51394.nevazio.data.model.ExpiryStatus
import dam.a51394.nevazio.data.model.ShoppingItem
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import androidx.lifecycle.viewModelScope
import dam.a51394.nevazio.data.repository.AuthRepository
import dam.a51394.nevazio.data.repository.FridgeRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ShoppingViewModel(
    private val authRepository: AuthRepository,
    private val fridgeRepository: FridgeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()
    private var currentFridgeId: String = "default_fridge"

    init {
        loadData()
    }

    private fun loadData() {
        val user = authRepository.currentUser

        viewModelScope.launch {
            if (user != null) {
                currentFridgeId = authRepository.getCurrentFridgeId()
            }

            fridgeRepository.getShoppingList(currentFridgeId).collectLatest { items ->
                val toBuy = items.filter { !it.bought }
                val bought = items.filter { it.bought }
                _uiState.update {
                    it.copy(
                        itemsToBuy = toBuy,
                        itemsBought = bought
                    )
                }
            }
        }
    }

    // ── Checkbox: mark as bought AND move to fridge ──────────────────────
    fun markAsBought(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                // Esta conversão existe para refletir o fluxo físico real: a marcação
                // de um item como comprado pressupõe a sua entrada imediata no frigorífico ou despensa.
                // O ShoppingItem é convertido num Ingredient, guardando-se o ID
                // gerado no ShoppingItem original (linkedIngredientId). Este comportamento garante a
                // possibilidade de reversão da ação, prevenindo problemas decorrentes de cliques acidentais.
                val newIngredient = Ingredient(
                    name = item.name,
                    quantity = "${item.quantity}${item.unit}",
                    expiryDate = item.expiryDate ?: Date(),
                    location = item.location,
                    iconName = dam.a51394.nevazio.ui.home.HomeViewModel.inferIconName(item.name),
                    status = ExpiryStatus.FRESH,
                    expiryLabel = "Novo"
                )
                
                fridgeRepository.markShoppingItemAsBought(currentFridgeId, item.id, newIngredient)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // ── Checkbox on bought item: undo (move back to "por comprar") ───────
    fun markAsNotBought(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                fridgeRepository.undoMarkShoppingItemAsBought(currentFridgeId, item.id, item.linkedIngredientId)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // ── Edit: open sheet pre-filled with item data ──────────────────────
    fun showEditSheet(item: ShoppingItem) {
        _uiState.update { it.copy(editingItem = item) }
    }

    fun hideEditSheet() {
        _uiState.update { it.copy(editingItem = null) }
    }

    fun updateItem(
        name: String,
        quantity: String,
        unit: String,
        location: StorageLocation,
        expiryMillis: Long?
    ) {
        val item = _uiState.value.editingItem ?: return
        val expiryDate = if (expiryMillis != null) Date(expiryMillis) else null

        viewModelScope.launch {
            try {
                fridgeRepository.updateShoppingItem(
                    currentFridgeId, 
                    item.id, 
                    mapOf(
                        "name" to name,
                        "quantity" to quantity,
                        "unit" to unit,
                        "location" to location.name,
                        "expiryDate" to expiryDate
                    )
                )
            } catch (e: Exception) {
                // Ignore
            }
            hideEditSheet()
        }
    }

    // ── Add dialog ──────────────────────────────────────────────────────
    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun addItem(
        name: String,
        quantity: String,
        unit: String,
        location: StorageLocation,
        expiryMillis: Long?
    ) {
        if (name.isBlank()) return

        val user = authRepository.currentUser
        viewModelScope.launch {
            var addedBy = user?.email?.split("@")?.firstOrNull() ?: "Eu"
            if (user != null) {
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val doc = db.collection("users").document(user.uid).get().await()
                    val realName = doc.getString("name")
                    if (!realName.isNullOrBlank()) {
                        addedBy = realName
                    }
                } catch (e: Exception) {
                    // Ignore, fallback to email
                }
            }

            val expiryDate = if (expiryMillis != null) Date(expiryMillis) else null

            val newItem = ShoppingItem(
                name = name,
                quantity = quantity,
                unit = unit,
                location = location,
                expiryDate = expiryDate,
                addedBy = addedBy
            )

            fridgeRepository.addShoppingItem(currentFridgeId, newItem)
            hideAddDialog()
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────
    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            fridgeRepository.removeShoppingItem(currentFridgeId, item.id)
        }
    }
}

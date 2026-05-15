package dam.a51394.nevazio.data.model

import java.util.Date

enum class StorageLocation {
    FRIDGE, PANTRY
}

enum class ExpiryStatus {
    FRESH, EXPIRING_SOON, EXPIRES_TODAY, EXPIRED
}

data class Ingredient(
    val id: String,
    val name: String,
    val quantity: String,
    val expiryDate: Date?,
    val location: StorageLocation,
    val iconName: String, // e.g. "egg", "water_drop", "eco"
    val status: ExpiryStatus,
    val expiryLabel: String = "" // e.g. "12 Mai", "Hoje!", "2 dias"
)

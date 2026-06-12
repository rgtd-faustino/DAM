package dam.a51394.nevazio.data.model

import java.util.Date

enum class StorageLocation {
    FRIDGE, PANTRY
}

enum class ExpiryStatus {
    FRESH, EXPIRING_SOON, EXPIRES_TODAY, EXPIRED
}

data class Ingredient(
    var id: String = "",
    var name: String = "",
    var quantity: String = "",
    var expiryDate: Date? = null,
    var location: StorageLocation = StorageLocation.FRIDGE,
    var iconName: String = "", // e.g. "egg", "water_drop", "eco"
    var status: ExpiryStatus = ExpiryStatus.FRESH,
    var expiryLabel: String = "" // e.g. "12 Mai", "Hoje!", "2 dias"
)

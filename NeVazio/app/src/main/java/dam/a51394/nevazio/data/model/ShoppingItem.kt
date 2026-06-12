package dam.a51394.nevazio.data.model

data class ShoppingItem(
    var id: String = "",
    var name: String = "",
    var quantity: String = "",
    var unit: String = "un",
    var location: StorageLocation = StorageLocation.FRIDGE,
    var expiryDate: java.util.Date? = null,
    var addedBy: String = "",
    var bought: Boolean = false,
    var linkedIngredientId: String = ""
)

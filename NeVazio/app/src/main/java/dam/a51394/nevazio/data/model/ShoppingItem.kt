package dam.a51394.nevazio.data.model

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: String,
    val addedBy: String,
    val isBought: Boolean = false
)

package dam.a51394.nevazio.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dam.a51394.nevazio.data.model.Ingredient
import dam.a51394.nevazio.data.model.ShoppingItem
import dam.a51394.nevazio.data.model.ExpiryUtils.withComputedExpiry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FridgeRepository {
    private val db = FirebaseFirestore.getInstance()
    
    // A arquitetura de sincronização em tempo real baseia-se na abertura de uma ligação contínua
    // (WebSocket) entre o dispositivo e os servidores da Firestore, estabelecida pelo addSnapshotListener.
    // Em vez de efetuar pedidos periódicos de dados (polling) — o que provocaria um consumo excessivo
    // de bateria e tráfego de rede — a aplicação subscreve passivamente os eventos da coleção. 
    // Quando ocorre uma mutação na base de dados (ex: outro dispositivo na mesma família adiciona um ingrediente),
    // o servidor efetua o "push" automático do novo snapshot. O fluxo de dados é então emitido de imediato
    // através deste callbackFlow, propagando a alteração para a interface sem necessidade de recarregamento manual.
    fun getIngredients(fridgeId: String): Flow<List<Ingredient>> = callbackFlow {
        val listener = db.collection("fridges").document(fridgeId).collection("ingredients")
            .addSnapshotListener { snapshot, e ->
                // O comportamento original utilizava close(e) na presença de erros. Contudo, perdas momentâneas
                // de internet ou falhas transientes de permissão causavam o encerramento total do fluxo,
                // deixando a interface sem atualizações para o resto do ciclo de vida da aplicação.
                // Com o retorno imediato ("return"), permite-se que a SDK do Firestore faça a gestão automática 
                // das reconexões, mantendo o fluxo vivo para a receção dos dados assim que a ligação estabilizar.
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ingredients = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Ingredient::class.java)?.apply { id = doc.id }?.withComputedExpiry()
                    }
                    trySend(ingredients)
                }
            }
        
        // O awaitClose é garantidamente chamado quando o Flow é cancelado (ex: o ViewModel é limpo).
        // Isto é crucial para não termos listeners "zombies" a gastar memória e quota no Firebase.
        awaitClose { listener.remove() }
    }
    
    suspend fun addIngredient(fridgeId: String, ingredient: Ingredient) {
        db.collection("fridges").document(fridgeId)
            .collection("ingredients")
            .add(ingredient)
            .await()
    }
    
    suspend fun removeIngredient(fridgeId: String, ingredientId: String) {
        db.collection("fridges").document(fridgeId)
            .collection("ingredients")
            .document(ingredientId)
            .delete()
            .await()
    }
    
    suspend fun updateIngredient(fridgeId: String, ingredientId: String, updates: Map<String, Any?>) {
        db.collection("fridges").document(fridgeId)
            .collection("ingredients")
            .document(ingredientId)
            .update(updates)
            .await()
    }
    
    fun getShoppingList(fridgeId: String): Flow<List<ShoppingItem>> = callbackFlow {
        val listener = db.collection("fridges").document(fridgeId).collection("shoppingList")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ShoppingItem::class.java)?.apply { id = doc.id }
                    }
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun addShoppingItem(fridgeId: String, item: ShoppingItem) {
        db.collection("fridges").document(fridgeId)
            .collection("shoppingList")
            .add(item)
            .await()
    }
    
    suspend fun toggleShoppingItem(fridgeId: String, itemId: String, isBought: Boolean) {
        db.collection("fridges").document(fridgeId)
            .collection("shoppingList")
            .document(itemId)
            .update("bought", isBought)
            .await()
    }
    
    suspend fun removeShoppingItem(fridgeId: String, itemId: String) {
        db.collection("fridges").document(fridgeId)
            .collection("shoppingList")
            .document(itemId)
            .delete()
            .await()
    }
    
    suspend fun addShoppingItemAsMap(fridgeId: String, itemId: String, itemMap: Map<String, Any?>) {
        db.collection("fridges").document(fridgeId)
            .collection("shoppingList")
            .document(itemId)
            .set(itemMap)
            .await()
    }

    suspend fun updateShoppingItem(fridgeId: String, itemId: String, updates: Map<String, Any?>) {
        db.collection("fridges").document(fridgeId)
            .collection("shoppingList")
            .document(itemId)
            .update(updates)
            .await()
    }

    suspend fun markShoppingItemAsBought(fridgeId: String, shoppingItemId: String, ingredient: Ingredient) {
        val docRef = db.collection("fridges").document(fridgeId).collection("ingredients").document()
        ingredient.id = docRef.id
        docRef.set(ingredient).await()

        db.collection("fridges").document(fridgeId)
            .collection("shoppingList").document(shoppingItemId)
            .update(
                mapOf(
                    "bought" to true,
                    "linkedIngredientId" to docRef.id
                )
            ).await()
    }

    suspend fun undoMarkShoppingItemAsBought(fridgeId: String, shoppingItemId: String, linkedIngredientId: String) {
        if (linkedIngredientId.isNotEmpty()) {
            removeIngredient(fridgeId, linkedIngredientId)
        }
        db.collection("fridges").document(fridgeId)
            .collection("shoppingList").document(shoppingItemId)
            .update(
                mapOf(
                    "bought" to false,
                    "linkedIngredientId" to ""
                )
            ).await()
    }
}

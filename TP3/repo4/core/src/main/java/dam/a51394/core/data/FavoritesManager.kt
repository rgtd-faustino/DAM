package dam.a51394.core.data

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dam.a51394.core.model.CatImage

class FavoritesManager(context: Context) {
    private val prefs = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getFavorites(): List<CatImage> {
        val json = prefs.getString("favorites_list", null) ?: return emptyList()
        val type = object : TypeToken<List<CatImage>>() {}.type
        return gson.fromJson(json, type)
    }

    fun isFavorite(catId: String): Boolean {
        return getFavorites().any { it.id == catId }
    }

    fun addFavorite(cat: CatImage, context: Context) {
        val currentFavs = getFavorites().toMutableList()
        
        if (currentFavs.any { it.id == cat.id }) {
            Toast.makeText(context, "Este gato já está nos favoritos!", Toast.LENGTH_SHORT).show()
            return
        }

        currentFavs.add(cat)

        // Lógica FIFO: Sempre que existam mais de 5, removemos o elemento mais antigo (índice 0)
        while (currentFavs.size > 5) {
            currentFavs.removeAt(0)
        }

        saveFavorites(currentFavs)
        Toast.makeText(context, "Gato adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
    }

    fun removeFavorite(catId: String, context: Context) {
        val currentFavs = getFavorites().toMutableList()
        val removed = currentFavs.removeAll { it.id == catId }
        
        if (removed) {
            saveFavorites(currentFavs)
            Toast.makeText(context, "Gato removido dos favoritos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFavorites(list: List<CatImage>) {
        val json = gson.toJson(list)
        prefs.edit().putString("favorites_list", json).apply()
    }
}

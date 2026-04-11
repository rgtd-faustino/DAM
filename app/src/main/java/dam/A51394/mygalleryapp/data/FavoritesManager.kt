package dam.A51394.mygalleryapp.data

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dam.A51394.mygalleryapp.model.CatImage

class FavoritesManager(context: Context) {
    private val prefs = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getFavorites(): List<CatImage> {
        val json = prefs.getString("favorites_list", null) ?: return emptyList()
        val type = object : TypeToken<List<CatImage>>() {}.type
        return gson.fromJson(json, type)
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

    private fun saveFavorites(list: List<CatImage>) {
        val json = gson.toJson(list)
        prefs.edit().putString("favorites_list", json).apply()
    }
}

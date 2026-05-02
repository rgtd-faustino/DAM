package dam.a51394.core.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dam.a51394.core.model.CatImage

class CacheManager(context: Context) {
    private val prefs = context.getSharedPreferences("cache_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCachedImages(): List<CatImage> {
        val json = prefs.getString("cached_images_list", null) ?: return emptyList()
        val type = object : TypeToken<List<CatImage>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addImagesToCache(newImages: List<CatImage>) {
        val currentCache = getCachedImages().toMutableList()
        
        // Evita duplicados sucessivos baseados no ID
        newImages.forEach { newCat ->
            if (currentCache.none { it.id == newCat.id }) {
                currentCache.add(newCat)
            }
        }

        // Lógica FIFO: Sempre que existam mais de 50 itens, removemos os mais antigos (índice 0)
        while (currentCache.size > 50) {
            currentCache.removeAt(0)
        }

        saveCache(currentCache)
    }

    private fun saveCache(list: List<CatImage>) {
        val json = gson.toJson(list)
        prefs.edit().putString("cached_images_list", json).apply()
    }
}

package dam.a51394.app_compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dam.a51394.core.data.FavoritesManager
import dam.a51394.core.model.CatImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)

    private val _favorites = MutableStateFlow<List<CatImage>>(emptyList())
    val favorites: StateFlow<List<CatImage>> = _favorites

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _favorites.value = favoritesManager.getFavorites()
    }

    fun addFavorite(cat: CatImage) {
        favoritesManager.addFavorite(cat, getApplication())
        loadFavorites()
    }

    fun removeFavorite(catId: String) {
        favoritesManager.removeFavorite(catId, getApplication())
        loadFavorites()
    }

    fun isFavorite(catId: String): Boolean {
        return favoritesManager.isFavorite(catId)
    }
}

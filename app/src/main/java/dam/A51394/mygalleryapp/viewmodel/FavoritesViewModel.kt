package dam.A51394.mygalleryapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dam.A51394.mygalleryapp.data.FavoritesManager
import dam.A51394.mygalleryapp.model.CatImage

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)
    
    private val _favorites = MutableLiveData<List<CatImage>>()
    val favorites: LiveData<List<CatImage>> get() = _favorites

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _favorites.value = favoritesManager.getFavorites()
    }

    fun removeFavorite(cat: CatImage) {
        favoritesManager.removeFavorite(cat.id, getApplication())
        loadFavorites() // Update list after removal
    }
}

package dam.A51394.appcompose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dam.a51394.core.model.CatImage
import dam.a51394.core.repository.CatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CatRepository(application)

    private val _catImages = MutableStateFlow<List<CatImage>>(emptyList())
    val catImages: StateFlow<List<CatImage>> = _catImages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchImages()
    }

    fun fetchImages() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getCatImages()
            result.onSuccess { _catImages.value = it }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }
}
package dam.A51394.mygalleryapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam.A51394.mygalleryapp.model.CatImage
import dam.A51394.mygalleryapp.repository.CatRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = CatRepository()

    private val _catImages = MutableLiveData<List<CatImage>>()
    val catImages: LiveData<List<CatImage>> get() = _catImages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        fetchImages()
    }

    fun fetchImages(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val images = repository.getCatImages(limit)
                _catImages.value = images
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar imagens: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

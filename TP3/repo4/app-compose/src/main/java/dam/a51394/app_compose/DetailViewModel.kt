package dam.a51394.app_compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dam.a51394.core.model.CatImage
import dam.a51394.core.repository.CatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CatRepository(application)

    private val _catImage = MutableStateFlow<CatImage?>(null)
    val catImage: StateFlow<CatImage?> = _catImage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchImageDetail(imageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getCatImageDetail(imageId)
            result.onSuccess { 
                _catImage.value = it 
            }.onFailure { 
                _errorMessage.value = it.message 
            }
            _isLoading.value = false
        }
    }
}

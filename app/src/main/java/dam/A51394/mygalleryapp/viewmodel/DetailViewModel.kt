package dam.A51394.mygalleryapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dam.A51394.mygalleryapp.model.CatImage
import dam.A51394.mygalleryapp.repository.CatRepository
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CatRepository(application)

    private val _imageDetail = MutableLiveData<CatImage?>()
    val imageDetail: LiveData<CatImage?> get() = _imageDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchImageDetails(imageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getCatImageDetail(imageId)
            
            result.onSuccess { image ->
                _imageDetail.value = image
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
            
            _isLoading.value = false
        }
    }
}

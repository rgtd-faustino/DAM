package dam.a51394.cooljetpackweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam.a51394.cooljetpackweatherapp.data.WeatherApiClient
import dam.a51394.cooljetpackweatherapp.ui.FavoriteLocation
import dam.a51394.cooljetpackweatherapp.ui.WeatherUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUIState())
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    fun updateLatitude(lat: Float) {
        _uiState.update { it.copy(latitude = lat) }
    }

    fun updateLongitude(lon: Float) {
        _uiState.update { it.copy(longitude = lon) }
    }


    fun addFavorite(name: String) {
        val lat = _uiState.value.latitude
        val lon = _uiState.value.longitude
        // criamos o objeto da classe que criamos no weather ui state
        val newFavorite = FavoriteLocation(name, lat, lon)

        // criamos uma lista nova e igual que possamos alterar para incluir o favorito
        val currentFavorites = _uiState.value.favorites.toMutableList()

        // se já existir um favorito com o mesmo nome não fazemos nada
        var alrExists = false
        for (favorite in currentFavorites) {
            if (favorite.name == name) {
                alrExists = true
            }
        }
        if (!alrExists) {
            currentFavorites.add(newFavorite)
            _uiState.update {
                it.copy(favorites = currentFavorites)
            }
        }
    }

    fun selectFavorite(favorite: FavoriteLocation) {
        // atualizamos as coordenadas com as do favorito selecionado
        _uiState.update {
            it.copy(latitude = favorite.latitude, longitude = favorite.longitude)
        }
        // e apanhamos os resultados
        fetchWeather()
    }

    fun removeFavorite(favorite: FavoriteLocation) {
        // copiamos a lista atual e removemos o favorito selecionado
        val currentFavorites = _uiState.value.favorites.toMutableList()
        currentFavorites.remove(favorite)
        _uiState.update { it.copy(favorites = currentFavorites) }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            val lat = _uiState.value.latitude
            val lon = _uiState.value.longitude
            val data = WeatherApiClient.getWeather(lat, lon)
            if (data != null) {
                val cw = data.current_weather
                val pressure = data.hourly.pressure_msl.firstOrNull()?.toFloat() ?: 0f

                // calcular se é dia ou noite
                val isDay = try {
                    val currentTime = cw.time  // formato "2025-03-26T14:45"
                    val sunrise = data.daily?.sunrise?.firstOrNull() ?: ""
                    val sunset = data.daily?.sunset?.firstOrNull() ?: ""
                    currentTime >= sunrise && currentTime < sunset
                } catch (e: Exception) {
                    true
                }

                _uiState.update {
                    it.copy(
                        temperature = cw.temperature,
                        windspeed = cw.windspeed,
                        winddirection = cw.winddirection,
                        weathercode = cw.weathercode,
                        seaLevelPressure = pressure,
                        time = cw.time,
                        isDay = isDay
                    )
                }
            }
        }
    }
}
package dam.a51394.cooljetpackweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam.a51394.cooljetpackweatherapp.data.WeatherApiClient
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
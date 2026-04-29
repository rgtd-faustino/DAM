package dam.a51394.cooljetpackweatherapp.ui

data class WeatherUIState(
    val latitude: Float = 38.7223f,
    val longitude: Float = -9.1393f,
    val temperature: Float = 0f,
    val windspeed: Float = 0f,
    val winddirection: Int = 0,
    val weathercode: Int = 0,
    val seaLevelPressure: Float = 0f,
    val time: String = "",
    val isDay: Boolean = true,
    // da mesma maneira que fizémos ocm as classes podemos adicionar aqui uma que tenha
    // os parametros latitude e longitude para definir a localização e metemos o nome para
    // sabermos qual é
    val favorites: List<FavoriteLocation> = emptyList()
)
data class FavoriteLocation(
    val name: String,
    val latitude: Float,
    val longitude: Float
)
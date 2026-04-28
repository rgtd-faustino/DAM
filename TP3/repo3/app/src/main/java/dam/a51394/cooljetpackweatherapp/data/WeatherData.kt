package dam.a51394.cooljetpackweatherapp.data

import kotlinx.serialization.Serializable

// data classes são usadas porque como estas variáveis representam os dados que
// a API nos vai fornecer, não serão usados com lógica por detrás, são literalmente
// contentores de dados que depois servirão para dar output
@Serializable
data class WeatherData(
    var latitude: Float,
    var longitude: Float,
    var timezone: String,
    var current_weather: CurrentWeather,
    var hourly: Hourly,
    // para trocar entre o tema dia e noite, volta se a repetir o sistema de classes que já usamos
    var daily: Daily
)

// basicamente se a hora atual estiver entre o nascer e pôr do sol então é dia, senão é noite
@Serializable
data class Daily(
    var sunrise: ArrayList<String>,
    var sunset: ArrayList<String>
)

// em vez de meter estas variáveis na classe WeatherData criamos outra para o código ficar
// mais intuitivo e simples de entender
@Serializable
data class CurrentWeather(
    var temperature: Float,
    var windspeed: Float,
    var winddirection: Int,
    var weathercode: Int,
    var time: String
)

// mesmo objetivo para esta classe
@Serializable
data class Hourly(
    var time: ArrayList<String>,
    var temperature_2m: ArrayList<Float>,
    var weathercode: ArrayList<Int>,
    var pressure_msl: ArrayList<Double>
)

// lista de todos os tipos possíveis de tempo metereológico que são possíveis com a API
// e a imagem correspondente
@Serializable
enum class WMO_WeatherCode(var code: Int, var image: String) {
    CLEAR_SKY(0, "clear_"),
    MAINLY_CLEAR(1, "mostly_clear_"),
    PARTLY_CLOUDY(2, "partly_cloudy_"),
    OVERCAST(3, "cloudy"),
    FOG(45, "fog"),
    DEPOSITING_RIME_FOG(48, "fog"),
    DRIZZLE_LIGHT(51, "drizzle"),
    DRIZZLE_MODERATE(53, "drizzle"),
    DRIZZLE_DENSE(55, "drizzle"),
    FREEZING_DRIZZLE_LIGHT(56, "freezing_drizzle"),
    FREEZING_DRIZZLE_DENSE(57, "freezing_drizzle"),
    RAIN_SLIGHT(61, "rain_light"),
    RAIN_MODERATE(63, "rain"),
    RAIN_HEAVY(65, "rain_heavy"),
    FREEZING_RAIN_LIGHT(66, "freezing_rain_light"),
    FREEZING_RAIN_HEAVY(67, "freezing_rain_heavy"),
    SNOW_FALL_SLIGHT(71, "snow_light"),
    SNOW_FALL_MODERATE(73, "snow"),
    SNOW_FALL_HEAVY(75, "snow_heavy"),
    SNOW_GRAINS(77, "snow"),
    RAIN_SHOWERS_SLIGHT(80, "rain_light"),
    RAIN_SHOWERS_MODERATE(81, "rain"),
    RAIN_SHOWERS_VIOLENT(82, "rain_heavy"),
    SNOW_SHOWERS_SLIGHT(85, "snow_light"),
    SNOW_SHOWERS_HEAVY(86, "snow_heavy"),
    THUNDERSTORM_SLIGHT_MODERATE(95, "tstorm"),
    THUNDERSTORM_HAIL_SLIGHT(96, "tstorm"),
    THUNDERSTORM_HAIL_HEAVY(99, "tstorm")
}

// este get retorna um dicionário que combina um código com um determinado tempo
// metereológico, usamos isto para saber que imagem depois usarmos na aplicação
fun getWeatherCodeMap(): Map<Int, WMO_WeatherCode> {
    val weatherMap = HashMap<Int, WMO_WeatherCode>()
    WMO_WeatherCode.values().forEach {
        weatherMap.put(it.code, it)
    }
    return weatherMap
}

/*// guarda a descrição e o nome da imagem de um código meteorológico
data class WeatherCodeInfo(
    val description: String,
    val image: String
)*/

/*// para não termos os dados hardcoded isto lê o array do XML de recursos e faz um dicionário onde
// associa cada código metereológico à descrição e imagem
fun getWeatherCodeMap(context: android.content.Context): Map<Int, WeatherCodeInfo> {
    val weatherMap = HashMap<Int, WeatherCodeInfo>()
    // cada item do array está no formato "código,descrição,imagem" então separamos pelas ','
    // para termos cada um das três partes
    context.resources.getStringArray(R.array.weather_codes).forEach { entry ->
        val parts = entry.split(",")
        weatherMap[parts[0].toInt()] = WeatherCodeInfo(parts[1], parts[2])
    }
    return weatherMap
}*/
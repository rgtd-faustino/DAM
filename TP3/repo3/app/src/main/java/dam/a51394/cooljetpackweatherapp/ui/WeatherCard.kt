package dam.a51394.cooljetpackweatherapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WeatherCard(
    seaLevelPressure: Float,
    windDirection: Int,
    windSpeed: Float,
    temperature: Float,
    time: String
) {
    Card(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // usamos os valores dos parâmetros que vamos receber do resultado
            WeatherRow(label = "Sea Level Pressure", value = "$seaLevelPressure hPa")
            WeatherRow(label = "Wind Direction", value = "$windDirection°")
            WeatherRow(label = "Wind Speed", value = "$windSpeed km/h")
            WeatherRow(label = "Temperature", value = "$temperature°C")
            WeatherRow(label = "Time", value = time)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherCardPreview() {
    // no preview passamos valores fixos só para ver se o design está bom
    WeatherCard(
        seaLevelPressure = 0.0f,
        windDirection = 296,
        windSpeed = 20.8f,
        temperature = 14.4f,
        time = "2025-03-26T14:45"
    )
}
package dam.a51394.cooljetpackweatherapp.ui

import android.widget.Space
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WeatherCard() {
    Card(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            WeatherRow(label = "Sea Level Pressure", value = "0.0 hPa")
            WeatherRow(label = "Wind Direction", value = "296°")
            WeatherRow(label = "Wind Speed", value = "20.8 km/h")
            WeatherRow(label = "Temperature", value = "14.4°C")
            WeatherRow(label = "Time", value = "2025-03-26T14:45")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WeatherCardPreview(){
    WeatherCard()
}
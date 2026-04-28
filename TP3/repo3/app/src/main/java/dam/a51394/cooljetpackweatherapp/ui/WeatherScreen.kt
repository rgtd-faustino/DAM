package dam.a51394.cooljetpackweatherapp.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam.a51394.cooljetpackweatherapp.R
import dam.a51394.cooljetpackweatherapp.data.WMO_WeatherCode
import dam.a51394.cooljetpackweatherapp.data.getWeatherCodeMap
import dam.a51394.cooljetpackweatherapp.viewmodel.WeatherViewModel

@Composable
fun WeatherUI ( weatherViewModel : WeatherViewModel = viewModel () ) {
    val weatherUIState by weatherViewModel . uiState . collectAsState ()
    val latitude = weatherUIState . latitude
    val longitude = weatherUIState . longitude
    val temperature = weatherUIState . temperature
    val windSpeed = weatherUIState . windspeed
    val windDirection = weatherUIState . winddirection
    val weathercode = weatherUIState . weathercode
    val seaLevelPressure = weatherUIState . seaLevelPressure
    val time = weatherUIState . time
    val configuration = LocalConfiguration . current
    val day = true // Must change this in the future
    val mapt = getWeatherCodeMap () ;
    val wCode = mapt . get ( weathercode )
    val wImage = when ( wCode ) {
        WMO_WeatherCode . CLEAR_SKY ,
        WMO_WeatherCode. MAINLY_CLEAR ,
        // tirei os espaços senão dava bug e não encontrava a imagem
        WMO_WeatherCode . PARTLY_CLOUDY -> if ( day ) wCode ?. image + "day"
        // tirei os espaços senão dava bug e não encontrava a imagem
        else wCode ?. image + "night"
        else -> wCode ?. image
    }
    val context = LocalContext . current
    val wIcon = context . resources . getIdentifier ( wImage , "drawable" , // tirar os espaços
        context . packageName )
    if ( configuration . orientation == Configuration . ORIENTATION_LANDSCAPE ) {
        LandscapeWeatherUI (
            wIcon ,
            latitude ,
            longitude ,
            temperature ,
            windSpeed ,
            windDirection ,
            weathercode ,
            seaLevelPressure ,
            time ,
            onLatitudeChange = {
                    newValue -> newValue . toFloatOrNull () ?. let {
                weatherViewModel . updateLatitude ( it ) }
            },
            onLongitudeChange = {
                newValue -> newValue . toFloatOrNull () ?. let {
                weatherViewModel . updateLongitude ( it ) }
            },
            onUpdateButtonClick = {
                weatherViewModel . fetchWeather ()
            }
        )
    } else {
        PortraitWeatherUI (
            wIcon ,
            latitude ,
            longitude ,
            temperature ,
            windSpeed ,
            windDirection ,
            weathercode ,
            seaLevelPressure ,
            time ,
            onLatitudeChange = {
                    newValue ->
                newValue . toFloatOrNull () ?. let {
                    weatherViewModel . updateLatitude ( it ) }
            },
            onLongitudeChange = {
                    newValue ->
                newValue . toFloatOrNull () ?. let {
                    weatherViewModel . updateLongitude ( it ) }
            },
            onUpdateButtonClick = {
                weatherViewModel . fetchWeather ()
            }
        )
    }
}
@Composable
fun PortraitWeatherUI(
    wIcon: Int,
    latitude: Float,
    longitude: Float,
    temperature: Float,
    windSpeed: Float,
    windDirection: Int,
    weathercode: Int,
    seaLevelPressure: Float,
    time: String,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            // para dar vertical scroll se o conteúdo sair do telemóvel
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {

        // só mostramos a imagem se o wIcon não for 0, porque o getIdentifier
        // retorna 0 quando não encontra o recurso e depois o preview não dá refresh
        if (wIcon != 0) {
            Image(
                painter = painterResource(id = wIcon),
                contentDescription = "Weather icon",
                modifier = Modifier.size(120.dp)
            )
        }

        // card com os campos de latitude e longitude para o utilizador
        // escrever as coordenadas que quiser
        CoordinatesCard(
            latitude = latitude,
            longitude = longitude,
            onLatitudeChange = onLatitudeChange,
            onLongitudeChange = onLongitudeChange
        )

        // card com os dados que vieram da API
        WeatherCard(
            seaLevelPressure = seaLevelPressure,
            windDirection = windDirection,
            windSpeed = windSpeed,
            temperature = temperature,
            time = time
        )

        Button(
            onClick = onUpdateButtonClick,
            modifier = Modifier
                .fillMaxWidth() // para ocupar a largura toda
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.update_weather))
        }
    }
}
@Composable
fun LandscapeWeatherUI(
    wIcon: Int,
    latitude: Float,
    longitude: Float,
    temperature: Float,
    windSpeed: Float,
    windDirection: Int,
    weathercode: Int,
    seaLevelPressure: Float,
    time: String,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onUpdateButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // aqui precisamos mesmo do scroll vertical porque não aparece tudo no ecrã ao mesmo tempo
            .verticalScroll(rememberScrollState())
            .padding(8.dp), // menos padding para aproveitar melhor o espaço
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // imagem mais pequena para não ocupar muito espaço
        if (wIcon != 0) {
            Image(
                painter = painterResource(id = wIcon),
                contentDescription = "Weather icon",
                modifier = Modifier.size(80.dp)
            )
        }

        // tentei meter o CoordinatesCard e o WeatherCard lado a lado numa Row
        // mas os cards tentavam ocupar a largura toda cada um e saíam para fora do ecrã
        // então deixei em Column igual ao portrait
        CoordinatesCard(
            latitude = latitude,
            longitude = longitude,
            onLatitudeChange = onLatitudeChange,
            onLongitudeChange = onLongitudeChange
        )

        WeatherCard(
            seaLevelPressure = seaLevelPressure,
            windDirection = windDirection,
            windSpeed = windSpeed,
            temperature = temperature,
            time = time
        )

        Button(
            onClick = onUpdateButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = stringResource(R.string.update_weather))
        }
    }
}
// previews para vermos o design para testar para ver se está bom
@Preview(showBackground = true)
@Composable
fun PortraitWeatherUIPreview() {
    PortraitWeatherUI(
        wIcon = 0,
        latitude = 38.7223f,
        longitude = -9.1393f,
        temperature = 14.4f,
        windSpeed = 20.8f,
        windDirection = 296,
        weathercode = 3,
        seaLevelPressure = 1013.0f,
        time = "2025-03-26T14:45",
        onLatitudeChange = {},
        onLongitudeChange = {},
        onUpdateButtonClick = {}
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun LandscapeWeatherUIPreview() {
    LandscapeWeatherUI(
        wIcon = 0,
        latitude = 38.7223f,
        longitude = -9.1393f,
        temperature = 14.4f,
        windSpeed = 20.8f,
        windDirection = 296,
        weathercode = 3,
        seaLevelPressure = 1013.0f,
        time = "2025-03-26T14:45",
        onLatitudeChange = {},
        onLongitudeChange = {},
        onUpdateButtonClick = {}
    )
}
package dam.a51394.cooljetpackweatherapp.ui

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val wIcon = context . resources . getIdentifier ( wImage , " drawable " ,
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
fun PortraitWeatherUI (
    wIcon : Int ,
    latitude : Float ,
    longitude : Float ,
    temperature : Float ,
    windSpeed : Float ,
    windDirection : Int ,
    weathercode : Int ,
    seaLevelPressure : Float ,
    time : String ,
    onLatitudeChange : ( String ) -> Unit ,
    onLongitudeChange : ( String ) -> Unit ,
    onUpdateButtonClick : () -> Unit ,
) {
// ToDo
}
@Composable
fun LandscapeWeatherUI (
    wIcon : Int ,
    latitude : Float ,
    longitude : Float ,
    temperature : Float ,
    windSpeed : Float ,
    windDirection : Int ,
    weathercode : Int ,
    seaLevelPressure : Float ,
    time : String ,
    onLatitudeChange : ( String ) -> Unit ,
    onLongitudeChange : ( String ) -> Unit ,
    onUpdateButtonClick : () -> Unit ,
    ) {
    // ToDo
}
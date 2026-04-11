package dam.A51394.coolweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    // depois estas variávels vão ser alteradas pela API
    private var day = true
    private var lastLat = 38.76f
    private var lastLon = -9.12f

    // o launcher tem de ser declarado aqui como campo e não dentro do onCreate porque o Android
    // exige que os launchers de permissão sejam registados antes do onCreate ser chamado,
    // caso contrário a app tem um erro de IllegalStateException
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // verificamos se pelo menos uma das permissões foi concedida (fine ou coarse)
        // fine = GPS preciso, coarse = localização aproximada por rede/wifi
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
            getLocationAndFetch()
        else
            // se o utilizador recusou, usamos os valores pré definidos que já estão nas variáveis
            fetchWeatherData(lastLat, lastLon).start()
    }

    // apanhamos a localização GPS atual e chamamos a API com essas coordenadas
    // a permissão já foi verificada antes de chamar esta função no onCreate e no launcher
    // por isso dizemos ao Android Studio para ignorar o aviso de permissão em falta
    @SuppressLint("MissingPermission")
    private fun getLocationAndFetch() {
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation
            .addOnSuccessListener { location ->
                // a location pode ser null se o dispositivo não consegue obter uma localização
                // então são usadas as coordenadas pré definidas
                if (location != null) {
                    lastLat = location.latitude.toFloat()
                    lastLon = location.longitude.toFloat()
                    findViewById<EditText>(R.id.latitudeValue).setText(lastLat.toString())
                    findViewById<EditText>(R.id.longitudeValue).setText(lastLon.toString())
                }
                fetchWeatherData(lastLat, lastLon).start()
            }
            // se alguma coisa falhar usamos as coordenadas pré definidas
            .addOnFailureListener {
                fetchWeatherData(lastLat, lastLon).start()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // restaura as coordenadas anteriores se existirem (após o recreate)
        lastLat = savedInstanceState?.getFloat("lat") ?: 38.76f
        lastLon = savedInstanceState?.getFloat("lon") ?: -9.12f
        day = savedInstanceState?.getBoolean("day") ?: true

        // este código não estava a mudar nada, quando fui ver apercebi-me que o layout que uso
        // tem que o background fica por cima do windowBackground, então a solução seria ou
        // mudar diretamente o fundo (mas assim não eram usados os themes) ou então remover os
        // backgrounds dos layouts e deixar que o código trate disso, escolhi a solução 2
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (day) setTheme(R.style.Theme_Day)
                else setTheme(R.style.Theme_Night)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) setTheme(R.style.Theme_Day_Land)
                else setTheme(R.style.Theme_Night_Land)
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val latInput = findViewById<EditText>(R.id.latitudeValue)
        val lonInput = findViewById<EditText>(R.id.longitudeValue)

        // restaura os valores dos EditTexts após o recreate
        latInput.setText(lastLat.toString())
        lonInput.setText(lastLon.toString())

        // usamos as coordenadas atuais do GPS do android que ligou a app e chamamos a API com elas
        // a não ser que estejamos a trocar os valores após já termos ligado a app
        if (savedInstanceState != null) {
            // se o savedInstanceState não é null significa que o onCreate foi chamado por causa
            // de um recreate então já temos as coordenadas guardadas
            fetchWeatherData(lastLat, lastLon).start()
        } else {
            // senão então é a primeira vez que a app abre e vemos se já temos permissão
            // para vermos a localização
            val hasPermission =
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasPermission)
                getLocationAndFetch()
            else
                // se não tivermos permissão pedimos
                locationPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
        }

        // e sempre que o utilizador carregar no botão de update a API é chamada outra vez
        val updateButton = findViewById<Button>(R.id.updateButton)
        updateButton.setOnClickListener {
            val lat = latInput.text.toString().toFloatOrNull()
            val lon = lonInput.text.toString().toFloatOrNull()

            // adicionalmente, quando tentei usar a app não sabia que valores meter então pensei
            // que limites poderiam dar jeito para não deixar o utilizador meter valores que não
            // existem (no meu caso a aplicação após clicar no update só fechava)
            if (lat == null || lon == null) {
                Toast.makeText(this, "Introduz valores numéricos válidos!",
                    Toast.LENGTH_SHORT).show()

            } else if (!(lat in -90f..90f && lon in -180f..180f)) {
                Toast.makeText(this, "Latitude deve estar entre -90 e 90 e " +
                        "Longitude entre -180 e 180!", Toast.LENGTH_SHORT).show()

            } else {
                lastLat = lat
                lastLon = lon
                fetchWeatherData(lat, lon).start()
            }
        }
    }

    // guarda as coordenadas antes do recreate para não perder os dados porque quando
    // o recreate é chamado a activity é destruída e criada do 0, ou seja, perdemos as
    // variáveis todas e voltam aos valores default, conseguimos evitar isso ao usar esta função
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat("lat", lastLat)
        outState.putFloat("lon", lastLon)
        outState.putBoolean("day", day)
    }

    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        // esta build string basicamente constrói o URL da API com as coordenadas que oferecemos
        // e que nos vai fornecer os dados que queremos
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&") // para ser sempre o tempo atual
            // valores do hourly que queremos
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
            append("&daily=sunrise,sunset&timezone=auto") // para o tema dia/noite
        }

        // depois aqui abre a ligação ao URL e converte o JSON para o objeto WeatherData através do Gson
        val url = URL(reqString)
        url.openStream().use {
            return Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
        }
    }

    // esta função cria uma thread separada (para não bloquear a UI) que chama a API
    private fun fetchWeatherData(lat: Float, long: Float): Thread {
        return Thread {
            val weather = WeatherAPI_Call(lat, long)
            updateUI(weather)
        }
    }

    // aparentemente o Android não permite atualizar a UI a partir de uma thread secundária por
    // motivos de segurança e como o fetchWeatherData corre numa thread separada
    // para não bloquear a UI precisamos do runOnUiThread para "voltar" à thread principal
    // para fazer as alterações à UI
    private fun updateUI(request: WeatherData) {
        runOnUiThread {
            val weatherImage = findViewById<ImageView>(R.id.weatherImage)
            val pressure = findViewById<TextView>(R.id.seaLevelPressureValue)
            val windDirection = findViewById<TextView>(R.id.windDirectionValue)
            val windSpeed = findViewById<TextView>(R.id.windSpeedValue)
            val temperature = findViewById<TextView>(R.id.temperatureValue)
            val time = findViewById<TextView>(R.id.timeValue)

            // atualiza os valores dos TextViews com os dados da API
            pressure.text = request.hourly.pressure_msl[12].toString() + " hPa"
            windDirection.text = request.current_weather.winddirection.toString()
            windSpeed.text = request.current_weather.windspeed.toString() + " km/h"
            temperature.text = request.current_weather.temperature.toString() + " ºC"
            time.text = request.current_weather.time

            // alterar entre o tema de dia e noite de acordo com os dados da API
            val sunrise = request.daily.sunrise[0]
            val sunset = request.daily.sunset[0]
            val currentTime = request.current_weather.time
            val newDay: Boolean
            if(currentTime in sunrise..sunset)
                newDay = true
            else
                newDay = false
            // aqui temos de meter o recreate para reiniciar a activity para aplicar o novo tema,
            // mas isso estava a causar um loop infinito na aplicação:
            // recreate -> onCreate -> API -> updateUI -> recreate
            // por isso só reiniciamos se o valor de day mudou porque assim quando a API voltar a
            // responder, como o newDay vai ser igual ao day o recreate já não é chamado
            if (newDay != day) {
                day = newDay
                recreate()
            }

            // obtém o código de tempo e a imagem correspondente
            val mapt = getWeatherCodeMap(this)
            val wCode = mapt[request.current_weather.weathercode]
            // imagens que terminam em "_" são as que têm versão de dia e noite (ex: clear_day, clear_night)
            // as restantes são iguais independentemente da hora do dia
            val wImage = if (wCode?.image?.endsWith("_") == true)
                if (day) wCode.image + "day" else wCode.image + "night"
            else
                wCode?.image
            /*val wImage = when (wCode) {
                WMO_WeatherCode.CLEAR_SKY,
                WMO_WeatherCode.MAINLY_CLEAR,
                    // para corresponder aos nomes das imagens nos drawables metemos day ou night no fim
                WMO_WeatherCode.PARTLY_CLOUDY -> if (day) wCode?.image + "day" else wCode?.image + "night"
                else -> wCode?.image
            }*/

            // mete a imagem de tempo correspondente
            val resID = resources.getIdentifier(wImage, "drawable", packageName)
            val drawable = this.getDrawable(resID)
            weatherImage.setImageDrawable(drawable)
        }
    }
}
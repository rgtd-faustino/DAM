package dam.A51394.coolweatherapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {

    private var day = false

    override fun onCreate(savedInstanceState: Bundle?) {

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
        }

        // depois aqui abre a ligação ao URL e converte o JSON para o objeto WeatherData através do Gson
        val url = URL(reqString)
        url.openStream().use {
            return Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
        }
    }
}
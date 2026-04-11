package dam.A51394.coolweatherapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

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
}
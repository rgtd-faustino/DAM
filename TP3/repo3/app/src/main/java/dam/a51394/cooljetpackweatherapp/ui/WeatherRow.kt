package dam.a51394.cooljetpackweatherapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WeatherRow(label: String, value: String) {
    Row(
        // com o fill max width faz com que use o espaço todo até ao fim
        // vertical para que as linhas tenhma um espacinho entre si
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        // depois se meter space between usa o espaço todo ate ao fim e assim
        // os valores aparecem depois
            horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label + ":")
        Text(text = value)
    }
}


@Preview(showBackground = true)
@Composable
fun WeatherRowPreview(){
    WeatherRow("Parâmetro", "valor")
}
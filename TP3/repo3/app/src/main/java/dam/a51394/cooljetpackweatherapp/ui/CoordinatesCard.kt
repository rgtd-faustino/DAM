package dam.a51394.cooljetpackweatherapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CoordinatesCard(
    latitude: Float,
    longitude: Float,
    // como o código do weatherScreen tem o código igual também metemos aqui
    // o lambda sempre que os valores mudarem
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit
) {
    Card(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            WeatherRow(label = "Coordinates", value = "ícone mundo")

            Text(text = "Latitude")
            OutlinedTextField(
                // mostra o valor atual que vem do viewmodel
                value = latitude.toString(),
                onValueChange = onLatitudeChange,
                placeholder = { Text("38.7223") },
                // para a caixa preencher o espaço todo da width
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text(text = "Longitude")
            OutlinedTextField(
                // mostra o valor atual que vem do viewmodel
                value = longitude.toString(),
                onValueChange = onLongitudeChange,
                placeholder = { Text("9.1393") },
                // para a caixa preencher o espaço todo da width
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoordinatesCardPreview() {
    // passamos valores hardcoded para ver como ficava o design
    CoordinatesCard(
        latitude = 38.7223f,
        longitude = 9.1393f,
        // parametros vazios só para não dar erro
        onLatitudeChange = {},
        onLongitudeChange = {}
    )
}
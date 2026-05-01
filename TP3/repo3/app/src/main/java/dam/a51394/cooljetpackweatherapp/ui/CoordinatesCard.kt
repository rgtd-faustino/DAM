package dam.a51394.cooljetpackweatherapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    onLongitudeChange: (String) -> Unit,
    // para o novo botão dos favoritos
    onAddFavorite: (String) -> Unit,
    onSelectFavorite: (FavoriteLocation) -> Unit,
    onRemoveFavorite: (FavoriteLocation) -> Unit,
    favorites: List<FavoriteLocation>,
) {
    // estado local para guardar o texto que o utilizador está a escrever
    // a chave (latitude/longitude) faz com que o texto atualize quando
    // selecionamos um favorito ou quando o utlizador introduz valores, porque o valor do viewmodel muda

    // sem chave o remember só corre uma vez e fica preso no valor inicial, então ao selecionar
    // um favorito (Lisboa: lat 38.7, lon 9.1) o campo não atualizava, mas com chave o
    // remember recria o estado local sempre que o Float do ViewModel muda, o que resolve os
    // favoritos, mas tinha um problema que era que ao apagar dígitos ("38.7223" -> "38.")
    // o ViewModel convertia "38." para 38.0f, o remember recriava o latText com "38.0" e o
    // campo resetava sozinho a meio da edição, o que era muito chato, mas pensei que era melhor do
    // que os favoritos não funcionarem, mas depois isto foi resolvido no comentário embaixo
    val latText = remember(latitude) { mutableStateOf(latitude.toString()) }
    val lonText = remember(longitude) { mutableStateOf(longitude.toString()) }
    val favoriteName = remember { mutableStateOf("") }

    Card(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // emoji do mundo codigo que encontrei
            WeatherRow(label = "Coordinates", value = "\uD83C\uDF0D")

            Text(text = "Latitude")
            OutlinedTextField(
                // usamos o latText.value (que é o estado local em String) em vez da latitude
                // (que é o Float do ViewModel) diretamente porque se usássemos o Float do ViewModel
                // diretamente fazia com que o Compose redesenhava o campo com o valor já convertido
                // (por exemplo "38.0") sempre que o utilizador apagava dígitos  o que era chato
                // porque aparecia lixo no meio da edição, então assim o estado local serve de
                // "buffer" entre o que o utilizador está a escrever e o valor que o ViewModel tem guardado
                value = latText.value,
                onValueChange = {
                    latText.value = it
                    onLatitudeChange(it)
                },
                placeholder = { Text("38.7223") },
                // para a caixa preencher o espaço todo da width
                modifier = Modifier.fillMaxWidth(),
                // por algumm motivo o deciaml não aparece o "-"
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Text(text = "Longitude")
            OutlinedTextField(
                // usamos o lonText.value (que é o estado local em String) em vez da longitude
                // (que é o Float do ViewModel) diretamente porque se usássemos o Float do ViewModel
                // diretamente fazia com que o Compose redesenhava o campo com o valor já convertido
                // (por exemplo "38.0") sempre que o utilizador apagava dígitos  o que era chato
                // porque aparecia lixo no meio da edição, então assim o estado local serve de
                // "buffer" entre o que o utilizador está a escrever e o valor que o ViewModel tem guardado
                value = lonText.value,
                onValueChange = {
                    lonText.value = it
                    onLongitudeChange(it)
                },
                placeholder = { Text("9.1393") },
                // para a caixa preencher o espaço todo da width
                modifier = Modifier.fillMaxWidth(),
                // por algum motivo o teclado decimal não aparece o "-"
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // linha com a caixa de texto para o nome do favorito e o botão para guardar
            Row(
                modifier = Modifier.padding(top = 8.dp),
                // para ficar no centro da caixa de input a nivel vertical
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = favoriteName.value,
                    onValueChange = { favoriteName.value = it },
                    placeholder = { Text("Nome do favorito") },
                    // o botão não estava a ficar bem formatado e a estrela ficava fora do sítio
                    // isto faz com que a caixa de texto ocupe todo o espaço disponível
                    // da row à exceção do espaço que o botão precisa para aparecer
                    // então não lhe rouba espaço
                    modifier = Modifier.weight(1f)
                )
                // botão para guardar o favorito com as coordenadas atuais
                Button(
                    onClick = {
                        // só guardamos se o nome não estiver vazio
                        if (favoriteName.value.isNotBlank()) {
                            onAddFavorite(favoriteName.value)
                            // para limpar a caixa depois de guardar
                            favoriteName.value = ""
                        }
                    },
                    // para afastar o botão da caixa de input
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("★")
                }
            }

            // lista horizontal de favoritos que só aparece se houverem favoritos guardados
            if (favorites.isNotEmpty()) {
                // a lazy row aqui fica bem porque só cria botões para caberem no ecrã ou seja
                // se tivermos 50 favoritos mas só cabem 4 então só mostramos 4
                // e aparentemente a lazyrow tem horizontal scroll por defeito, reparei que tinha
                // quando consegui dar scroll nos botões
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    // espaço entre os botões dos favoritos
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // para cada favorito na lista criamos um botão para os selecionar
                    items(favorites) { favorite ->
                        Button(onClick = { onSelectFavorite(favorite) }) {
                            Text(favorite.name)
                        }
                        Button(
                            onClick = { onRemoveFavorite(favorite) },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text("x")
                        }
                    }

                }
            }
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
        onLongitudeChange = {},
        onAddFavorite = {},
        onSelectFavorite = {},
        onRemoveFavorite = {},
        favorites = emptyList()
    )
}
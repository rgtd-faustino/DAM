# Assignment 3 — Android MVVM e Jetpack Compose

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 03/05/2026  
**Repository URL:** [DAM_TP3_Android](https://github.com/rgtd-faustino/DAM/tree/main/TP3/repo3)

---

## 1. Introdução

Este relatório descreve o desenvolvimento do exercício Android da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM) do ISEL. O objetivo foi reconstruir a WeatherApp do tutorial anterior, desta vez seguindo a arquitetura MVVM (Model-View-ViewModel) e substituindo os layouts XML por Jetpack Compose.

O projeto foi implementado como uma aplicação Android organizada em três packages com responsabilidades separadas: `data`, `viewmodel` e `ui`. A interface foi construída inteiramente em Compose, com suporte a orientação portrait e landscape, tema dia/noite consoante a hora real, e gestão de localizações favoritas.

---

## 2. Visão Geral do Sistema

O projeto está organizado da seguinte forma:

- **`data`** — Contém `WeatherData.kt` com as data classes anotadas com `@Serializable`, `WeatherApiClient.kt` com o cliente HTTP baseado em Ktor, e a enum `WMO_WeatherCode` com todos os códigos meteorológicos suportados pela API.
- **`viewmodel`** — Contém `WeatherViewModel.kt`, responsável por gerir o estado da UI e comunicar com a camada de dados.
- **`ui`** — Contém todos os composables: `WeatherScreen.kt`, `CoordinatesCard.kt`, `WeatherCard.kt`, `WeatherRow.kt` e `WeatherUIState.kt`.

O fluxo de dados segue o padrão MVVM: a UI observa o `uiState` exposto pelo ViewModel como `StateFlow`, e envia eventos de volta ao ViewModel (mudança de coordenadas, pedido de atualização, gestão de favoritos). O ViewModel comunica com o `WeatherApiClient` e atualiza o estado, o que faz com que a UI redesenhe automaticamente.

---

## 3. Arquitetura e Design

### Camada de dados

O `WeatherApiClient` é um `object` singleton que usa o cliente HTTP do Ktor com `ContentNegotiation` e deserialização JSON via `kotlinx.serialization`. O URL de pedido é construído com `buildString` e inclui os parâmetros `current_weather`, `hourly` (para pressão), `daily` (para nascer e pôr do sol) e `timezone=auto`.

As data classes seguem a estrutura do JSON devolvido pela API Open-Meteo. A classe `Daily` foi adicionada em relação ao tutorial anterior para permitir determinar se é dia ou noite, comparando a hora atual com os valores de `sunrise` e `sunset`.

### ViewModel

O `WeatherViewModel` expõe o estado da UI através de um `MutableStateFlow<WeatherUIState>` privado, convertido para `StateFlow` imutável com `asStateFlow()`. As operações disponíveis são:

- `updateLatitude` / `updateLongitude` — atualizam as coordenadas no estado.
- `fetchWeather` — lança uma coroutine no `viewModelScope` para chamar a API e atualizar o estado com os dados recebidos.
- `addFavorite` — adiciona uma localização favorita com o nome e as coordenadas atuais, verificando duplicados pelo nome.
- `selectFavorite` — atualiza as coordenadas com as do favorito selecionado e chama `fetchWeather` automaticamente.
- `removeFavorite` — remove um favorito da lista.

O cálculo de dia/noite é feito no `fetchWeather`: compara a hora da `current_weather` com os valores de `sunrise` e `sunset` da resposta da API, e guarda o resultado no estado como `isDay: Boolean`.

### Interface de utilizador

A UI está dividida em composables reutilizáveis:

- **`WeatherUI`** — Composable de topo que obtém o ViewModel, recolhe o estado e decide entre `PortraitWeatherUI` e `LandscapeWeatherUI` com base na orientação do dispositivo.
- **`PortraitWeatherUI` / `LandscapeWeatherUI`** — Composables sem estado que recebem todos os dados e callbacks como parâmetros e organizam os componentes no ecrã.
- **`CoordinatesCard`** — Card com os campos de latitude e longitude e a gestão de favoritos.
- **`WeatherCard`** — Card com os dados meteorológicos recebidos da API.
- **`WeatherRow`** — Linha simples com label e valor em `SpaceBetween`.

A imagem meteorológica é determinada a partir do `weathercode` devolvido pela API: é feita uma consulta ao mapa gerado por `getWeatherCodeMap()`, e o nome da imagem é construído dinamicamente adicionando o sufixo `"day"` ou `"night"` conforme o valor de `isDay` para os códigos que têm variantes (céu limpo, maioritariamente limpo, parcialmente nublado). O identificador do recurso drawable é obtido com `context.resources.getIdentifier()`.

### Gestão de favoritos

O `WeatherUIState` inclui uma propriedade `favorites: List<FavoriteLocation>`, onde `FavoriteLocation` é uma data class com `name`, `latitude` e `longitude`. O `CoordinatesCard` tem uma caixa de texto para o nome e um botão de estrela para guardar o favorito com as coordenadas atuais. Os favoritos guardados aparecem numa `LazyRow` com scroll horizontal, cada um com um botão para selecionar e outro para remover.

Uma decisão relevante aqui foi o uso de `remember(latitude)` e `remember(longitude)` nos campos de texto do `CoordinatesCard`. Sem a chave, o `remember` só corria uma vez e os campos não atualizavam ao selecionar um favorito. Com a chave, o estado local é recriado sempre que o Float do ViewModel muda. O problema que isto introduzia era que ao apagar dígitos ("38.7" → "38.") o campo resetava, porque o ViewModel convertia "38." para 38.0f e o `remember` recriava o texto com "38.0". A solução foi usar um estado local como buffer: o campo de texto mostra sempre o estado local em String, e só propaga para o ViewModel quando o valor é convertível para Float com `toFloatOrNull()`.

---

## 4. Implementação

### Camada de dados

| Ficheiro | Tipo | Descrição |
|---|---|---|
| `WeatherData.kt` | data classes + enum | Estrutura dos dados da API e mapeamento de códigos meteorológicos |
| `WeatherApiClient.kt` | object | Cliente HTTP Ktor para chamadas à API Open-Meteo |

### ViewModel

| Função | Descrição |
|---|---|
| `updateLatitude / updateLongitude` | Atualizam as coordenadas no estado |
| `fetchWeather` | Chama a API e atualiza o estado com os dados recebidos |
| `addFavorite` | Adiciona uma localização favorita sem duplicados |
| `selectFavorite` | Aplica as coordenadas do favorito e atualiza o tempo |
| `removeFavorite` | Remove um favorito da lista |

### UI

| Composable | Descrição |
|---|---|
| `WeatherUI` | Ponto de entrada da UI, gere orientação e ViewModel |
| `PortraitWeatherUI` | Layout em coluna para orientação vertical |
| `LandscapeWeatherUI` | Layout adaptado para orientação horizontal |
| `CoordinatesCard` | Campos de coordenadas e gestão de favoritos |
| `WeatherCard` | Dados meteorológicos recebidos da API |
| `WeatherRow` | Linha reutilizável com label e valor |

---

## 5. Testes e Validação

A validação foi feita manualmente em emulador Android.

- **Dados meteorológicos:** Confirmado que ao introduzir coordenadas e carregar em "Update Weather", os dados são atualizados corretamente com os valores da API.
- **Orientação:** Verificado que a aplicação adapta o layout corretamente ao rodar o dispositivo, sem perda de estado.
- **Tema dia/noite:** Confirmado que a imagem meteorológica muda entre variante de dia e de noite consoante a hora atual em relação ao nascer e pôr do sol da localização.
- **Favoritos:** Testado adicionar favoritos com o mesmo nome (não duplica), selecionar um favorito (atualiza coordenadas e vai buscar dados), e remover favoritos.
- **Scroll:** Verificado scroll vertical em portrait e landscape quando o conteúdo excede o ecrã, e scroll horizontal na lista de favoritos.

---

## 6. Instruções de Utilização

### Pré-requisitos

- Android Studio Hedgehog ou superior
- Android SDK 26+
- Dispositivo ou emulador Android

### Executar o projeto

Clonar o repositório:
```bash
git clone https://github.com/rgtd-faustino/DAM
cd DAM/TP3/repo2
```

Abrir o projeto no Android Studio e correr no emulador ou dispositivo físico com o botão Run.

A aplicação requer ligação à internet para comunicar com a API Open-Meteo.

### Estrutura de packages

```
com.example.cooljetpackweatherapp/
├── data/
│   ├── WeatherApiClient.kt
│   └── WeatherData.kt
├── viewmodel/
│   └── WeatherViewModel.kt
├── ui/
│   ├── CoordinatesCard.kt
│   ├── WeatherCard.kt
│   ├── WeatherRow.kt
│   ├── WeatherScreen.kt
│   ├── WeatherUIState.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt
```

---

# Autonomous Software Engineering Sections

> As secções 7 a 11 não se aplicam a este trabalho. O código foi desenvolvido com **[AC YES, AI NO]**, pelo que o autocomplete foi permitido mas não houve recurso a ferramentas de geração de código por IA. A IA foi utilizada apenas na redação do relatório (ver secção 15).

---

# Development Process

## 12. Version Control and Commit History

Os commits foram feitos de forma incremental, começando pela estrutura base MVVM e pela integração com a API, e evoluindo depois para as funcionalidades adicionais como o tema dia/noite e os favoritos.

---

## 13. Difficulties and Lessons Learned

- **`remember` com chave vs. sem chave:** O problema dos campos de texto não atualizarem ao selecionar favoritos levou à descoberta de que o `remember` sem chave só corre uma vez. Adicionar a latitude/longitude como chave resolve os favoritos, mas introduz o problema do reset a meio da edição. A solução foi usar um estado local como buffer entre o que o utilizador escreve e o valor guardado no ViewModel.

- **Teclado numérico e o símbolo "-":** Usar `KeyboardType.Decimal` no campo de longitude não mostrava o símbolo "-", o que impossibilitava introduzir coordenadas negativas. A solução foi mudar para `KeyboardType.Text`, que não é o ideal mas resolve o problema.

- **`getIdentifier` e espaços no nome da imagem:** O nome da imagem era construído com espaços entre partes do código, o que fazia com que `getIdentifier` não encontrasse o recurso e devolvesse 0. Remover os espaços da concatenação resolveu o problema.

- **Layout landscape com `Row`:** A primeira tentativa de colocar `CoordinatesCard` e `WeatherCard` lado a lado numa `Row` falhava porque cada card tentava ocupar a largura toda. Ficou em `Column` igual ao portrait, o que funciona bem com scroll vertical.

---

## 14. Future Improvements

- **LocationPickerActivity:** Adicionar a atividade de seleção de localização no mapa com Google Maps, para o utilizador poder escolher coordenadas sem as ter de escrever manualmente.
- **Persistência de favoritos:** Guardar os favoritos em `SharedPreferences` ou numa base de dados Room para que não se percam ao fechar a aplicação.
- **Internacionalização completa:** Garantir que todas as strings visíveis estão em `strings.xml` e que existe uma tradução completa em português.
- **Indicador de carregamento:** Mostrar um spinner ou skeleton enquanto os dados da API estão a ser carregados.
- **Tratamento de erros na UI:** Mostrar uma mensagem ao utilizador quando a chamada à API falha, em vez de simplesmente não atualizar os dados.

---

## 15. AI Usage Disclosure

**Código: [AC YES, AI NO]**  
O código foi desenvolvido pelo aluno com recurso a autocomplete do IDE. Não foram utilizadas ferramentas de geração de código por inteligência artificial. Todo o código foi escrito e revisto manualmente.

**Relatório: [AC YES, AI YES]**  
A redação e estruturação deste relatório foram assistidas pelo modelo **Claude (Anthropic)**. O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.
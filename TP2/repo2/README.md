# Assignment 2 — CoolWeather App

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 12/04/2026  
**Repository URL:** [DAM_TP1_CoolWeatherApp](https://github.com/rgtd-faustino/DAM/edit/main/TP2/repo2/)

---

## 1. Introdução

Esta aplicação foi desenvolvida no âmbito da unidade curricular de DAM e representa um sistema de consulta meteorológica em tempo real. O objetivo principal é integrar o consumo de APIs externas (Open-Meteo) com funcionalidades nativas de geolocalização do Android, proporcionando uma experiência de utilizador dinâmica que se adapta tanto à localização física do dispositivo como ao ciclo solar (dia/noite).

Ao contrário de projetos anteriores estáticos, o CoolWeather utiliza concorrência (threads) para operações de rede e um sistema de temas dinâmicos que altera a interface visual da aplicação sem intervenção manual do utilizador, baseando-se nos dados reais de nascer e pôr do sol da API.

## 2. Visão Geral do Sistema

A aplicação opera num ecrã principal que permite ao utilizador visualizar:
- Temperatura atual, pressão atmosférica e velocidade/direção do vento.
- Uma representação visual do tempo (sol, chuva, nuvens, etc.) através de ícones dinâmicos.
- Localização baseada no GPS do dispositivo ou introdução manual de coordenadas (Latitude/Longitude).

### Funcionalidades Principais:
- **Geolocalização Automática**: Ao iniciar, a app solicita permissão ao utilizador para aceder à `lastLocation` através do `FusedLocationProviderClient`.
- **Temas Inteligentes**: A interface alterna automaticamente entre o `Theme_Day` e `Theme_Night` (e as suas variantes landscape) comparando a hora atual com os dados de `sunrise` e `sunset` retornados pela API.
- **Validação de Inputs**: O sistema impede a submissão de coordenadas inválidas (fora do intervalo [-90, 90] para latitude e [-180, 180] para longitude).
- **Persistência de Estado**: Graças ao `onSaveInstanceState`, a aplicação mantém as últimas coordenadas e o estado do tema mesmo após rotação do ecrã ou reconstrução da Activity.

## 3. Arquitetura e Design

### Estrutura de UI
O layout foi desenhado para ser responsivo e informativo, utilizando as seguintes convenções:
- **CardViews**: Utilizados para agrupar dados relacionados (Pressão, Vento, Temperatura), conferindo profundidade e organização visual.
- **ConstraintLayout**: Serve de base a toda a interface para garantir flexibilidade e evitar sobreposições em diferentes densidades de ecrã.
- **Diferenciação de Orientação**: A aplicação suporta layouts específicos para Portrait e Landscape, otimizando o espaço disponível (ex: organização horizontal de cartões em landscape).

### Organização de Ficheiros
```
app/src/main/java/dam/A51394/coolweatherapp/
├── MainActivity.kt      # Lógica de UI, Permissões, Ciclo de Vida e Threads
└── WeatherData.kt       # Data Classes (GSON) e Mapeamento de Códigos WMO
```

## 4. Implementação

### Consumo da API e Parsing JSON
A comunicação com a API Open-Meteo é feita de forma assíncrona para não bloquear a Main Thread. O URL é construído dinamicamente com as coordenadas `lastLat` e `lastLon`:

```kotlin
private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
    val reqString = buildString {
        append("https://api.open-meteo.com/v1/forecast?")
        append("latitude=${lat}&longitude=${long}&")
        append("current_weather=true&hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
        append("&daily=sunrise,sunset&timezone=auto")
    }
    val url = URL(reqString)
    url.openStream().use {
        return Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
    }
}
```

Os dados são convertidos em objetos Kotlin através da biblioteca **Gson**, utilizando uma hierarquia de `data classes` definida em `WeatherData.kt`.

### Gestão de Threads e Atualização de UI
Devido às restrições do Android, a atualização dos componentes visuais (TextViews, ImageViews) após a receção dos dados da API é encapsulada em `runOnUiThread {}`:

```kotlin
private fun updateUI(request: WeatherData) {
    runOnUiThread {
        // ... atualização de TextViews ...
        if (newDay != day) {
            day = newDay
            recreate() // Aplica o novo tema dinâmico
        }
    }
}
```

### Temas Dinâmicos e Recursos
O mapeamento entre os códigos meteorológicos da WMO (World Meteorological Organization) e os recursos visuais é feito através de um ficheiro XML (`res/values/weather_codes.xml`) e de um `HashMap` construído em tempo de execução. Isto permite que a aplicação seja facilmente extensível com novos ícones ou descrições sem alterar o código principal.

## 5. Testes e Validação

Foram realizados testes exaustivos no emulador do Android Studio, focados em:
1.  **Simulação de GPS**: Alteração das coordenadas no AVD Manager para verificar a atualização automática.
2.  **Transição de Temas**: Testes com localizações em diferentes fusos horários (onde é noite ou dia) para validar a aplicação correta dos estilos Dark/Light.
3.  **Robustez de Dados**: Introdução de valores nulos, caracteres alfabéticos e coordenadas fora dos limites geográficos nos campos de texto, validando o feedback por `Toast`.
4.  **Ciclo de Vida**: Verificação de que os dados se mantêm durante rotações bruscas do dispositivo.

## 6. Instruções de Utilização

### Requisitos Mínimos
- Android SDK 24 (Android 7.0) ou superior.
- Conectividade à Internet (para chamadas à API).
- Google Play Services (para funcionalidades de localização).

### Instalação
1. Clonar: `git clone https://github.com/rgtd-faustino/DAM_TP1_CoolWeather.git`
2. Abrir no Android Studio.
3. Sincronizar o Gradle e descarregar as dependências (Gson, Google Play Services Location).
4. Executar (▶) num dispositivo ou emulador.

---

# Development Process

## 12. Version Control and Commit History

O projeto seguiu uma gestão de versões em branch única (`main`), com commits granulares que marcam a evolução desde o layout base até à implementação final das threads e localização. A utilização de nomes descritivos em português nos comentários de commit facilitou o rastreamento de bugs durante a fase de integração da API.

## 13. Difficulties and Lessons Learned

**O desafio do `recreate()`**:  
Uma das maiores dificuldades foi implementar a troca de tema sem criar um loop infinito. Inicialmente, o `recreate()` era chamado sempre que a API respondia, o que causava a destruição e recriação da activity, que por sua vez voltava a chamar a API. A solução passou por implementar uma verificação de estado (`if (newDay != day)`), garantindo que a atividade só é recreada se houver efetivamente uma mudança no ciclo solar.

**Segurança de Threads**:  
Aprendeu-se a importância de não manipular a interface gráfica a partir de threads secundárias, consolidando o conhecimento sobre o modelo de thread única do Android e o uso correto de `runOnUiThread`.

## 14. Future Improvements

Para versões futuras, o projeto beneficiaria das seguintes melhorias:
- **Previsão de 7 Dias**: Expandir o UI para apresentar uma lista (RecyclerView) com a previsão para a semana inteira.
- **Geocoding Inverso**: Permitir a pesquisa pelo nome da cidade (ex: "Lisboa") em vez de coordenadas decimais, utilizando o `Geocoder` do Android.
- **Cache Local**: Armazenar os últimos resultados num banco de dados Room para que a aplicação mostre dados, mesmo sem internet.

---

## 15. AI Usage Disclosure

**Código: [AC YES, AI NO]**  
Todo o código foi desenvolvido inteiramente pelo aluno sem recurso a ferramentas de geração de código por inteligência artificial. Apenas o autocomplete nativo do Android Studio foi utilizado.

**Relatório: [AC YES, AI YES]**  
A redação e estruturação deste relatório foi assistida pelo modelo **Antigravity (Google DeepMind)**. O aluno é totalmente responsável pelo conteúdo e confirma que o mesmo reflete o trabalho efetivamente realizado.

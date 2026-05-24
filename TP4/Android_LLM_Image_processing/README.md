# Assignment — Android LLM Image Processing

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 24/05/2026  
**Repository URL:** [DAM_TP4_Android_LLM_Image_Processing](https://github.com/rgtd-faustino/DAM/tree/main/TP4/Android_LLM_Image_processing)

---

## High-Level Project Description

Aplicação Android desenvolvida no âmbito da Task 3.5 do Tutorial 4 de DAM. Parte do projeto modelo *Gemini API Starter* fornecido pelo Android Studio e estende-o com uma funcionalidade adicional: um jogo de adivinha de preços de bolos, onde o utilizador arrisca um valor e recebe uma resposta do Gemini na pele de um chef de pastelaria.

---

## 1. Introdução

Este relatório descreve o desenvolvimento da Task 3.5 da unidade curricular de DAM, que consistiu em criar e estender uma aplicação Android com integração direta à API do Google Gemini para processamento de imagens. O ponto de partida foi o projeto *Gemini API Starter* do Android Studio, que já fornecia a estrutura base para enviar uma imagem e um prompt ao modelo e apresentar a resposta.

A funcionalidade extra implementada foi um jogo de adivinha de preços: o utilizador seleciona uma das três imagens de bolos disponíveis, escreve o que acha que esse bolo custa numa pastelaria, e o Gemini responde como chef de pastelaria a avaliar se o utilizador acertou, exagerou, ou ficou abaixo do valor real.

---

## 2. Visão Geral do Sistema

A aplicação tem um único ecrã (`BakingScreen`) com os seguintes elementos:

- **Galeria horizontal** com três imagens de bolos: cupcake, bolachas e bolo de aniversário.
- **Campo de prompt + botão Go** — funcionalidade original do starter; envia um prompt livre com a imagem selecionada ao Gemini.
- **Campo de preço + botão 💰 Guess** — funcionalidade extra; envia a imagem e o palpite de preço ao Gemini com um prompt de sistema que condiciona o modelo a responder como chef de pastelaria.
- **Área de resultado** — mostra um indicador de carregamento enquanto aguarda resposta, e o texto da resposta (ou mensagem de erro) quando termina.

O estado da UI é gerido por `UiState`, uma sealed interface com quatro estados: `Initial`, `Loading`, `Success` e `Error`.

---

## 3. Arquitetura e Design

A arquitetura segue o padrão MVVM com `BakingViewModel` a expor um `StateFlow<UiState>` observado pelo composable via `collectAsState()`.

**Backing Property Pattern no ViewModel:**

```kotlin
private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

O mesmo padrão aplicado no Step 2 do Tutorial de Flows aparece aqui de forma natural — o `MutableStateFlow` privado é o único ponto de escrita, e a interface pública é somente de leitura.

**Modelo Gemini:**

```kotlin
private val generativeModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = BuildConfig.apiKey
)
```

As chamadas ao modelo correm em `Dispatchers.IO` dentro de `viewModelScope`, garantindo que a thread principal não fica bloqueada.

**Prompt de sistema para o jogo:**

O método `guessPrice()` condiciona o modelo através do prompt antes da pergunta do utilizador, instruindo-o a comportar-se como chef de pastelaria com conhecimento de preços:

```kotlin
text("""
    You are a professional pastry chef and bakery pricing expert.
    The user thinks this baked good costs $userGuess.
    Analyze the image and estimate the real price in a bakery.
    Then tell the user if they guessed too high, too low, or about right.
    Keep it fun and short.
""".trimIndent())
```

O `trimIndent()` remove as indentações desnecessárias introduzidas pelo alinhamento do código, garantindo que o prompt chega ao modelo limpo.

**Estado persistente na UI:**

O campo de palpite de preço usa `rememberSaveable` em vez de `remember`, para que o valor não se perca em rotações de ecrã ou recomposições:

```kotlin
var userPriceGuess by rememberSaveable { mutableStateOf("") }
```

---

## 4. Implementação

| Ficheiro | Descrição |
|---|---|
| `MainActivity.kt` | Entry point; configura o tema e lança o `BakingScreen` |
| `BakingScreen.kt` | UI completa em Compose; galeria, campos de input, botões, resultado |
| `BakingViewModel.kt` | Lógica de chamada ao Gemini; `sendPrompt()` e `guessPrice()` |
| `UiState.kt` | Sealed interface com os quatro estados possíveis da UI |

---

## 5. Testes e Validação

Os testes foram feitos manualmente no emulador com as três imagens disponíveis:

- **Prompt livre**: confirmado que o Gemini responde corretamente a perguntas como "What is this?" e "Give me the recipe for this".
- **Jogo de preços — palpite baixo**: enviado "€0.50" para o cupcake; o Gemini respondeu que o valor estava muito abaixo do esperado e estimou um preço mais realista.
- **Jogo de preços — palpite alto**: enviado "€50" para as bolachas; o Gemini respondeu de forma humorística que o utilizador estava a exagerar.
- **Estado de loading**: confirmado que o `CircularProgressIndicator` aparece enquanto a resposta está a ser gerada.
- **Estado de erro**: simulado com chave de API inválida; a mensagem de erro é apresentada na área de resultado com a cor de erro do tema.
- **Rotação de ecrã**: confirmado que o campo de palpite não se perde graças ao `rememberSaveable`.

---

## 6. Instruções de Utilização

**Pré-requisitos**

- Android Studio Hedgehog ou superior
- Android SDK API 24+
- Chave de API do Google Gemini (obtida em [ai.google.dev](https://ai.google.dev/gemini-api/docs/api-key))

**Configuração**

No ficheiro `local.properties` na raiz do projeto, adicionar:

```
apiKey=a_tua_chave_aqui
```

A chave é injetada em `BuildConfig.apiKey` via `build.gradle` e nunca é exposta no código fonte.

**Execução**

Sincronizar o Gradle e correr no emulador ou dispositivo físico com o botão Run no Android Studio.

**Estrutura do projeto**

```
app/src/main/
├── java/dam/a51394/android_llm_image_processing/
│   ├── MainActivity.kt
│   ├── BakingScreen.kt
│   ├── BakingViewModel.kt
│   └── UiState.kt
├── res/
│   ├── drawable/
│   │   ├── baked_goods_1.jpg   # cupcake
│   │   ├── baked_goods_2.jpg   # bolachas
│   │   └── baked_goods_3.jpg   # bolo
│   └── values/
│       └── strings.xml
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

---

## Autonomous Software Engineering Sections

### 7. Prompting Strategy

O desenvolvimento da funcionalidade extra utilizou o Claude (Anthropic), acedido através da aplicação Antigravity, para auxiliar na definição do prompt de sistema e na estrutura do método `guessPrice()`.

| Componente | Descrição |
|---|---|
| Context | Aplicação Android com Gemini 2.0 Flash e processamento de imagens |
| Goal | Adicionar um jogo de adivinha de preços com resposta condicionada pelo prompt |
| Constraints | Manter a estrutura existente do starter; não duplicar lógica do `sendPrompt()`; resposta curta e divertida |
| Plan | Adicionar campo de input + botão na UI; novo método no ViewModel com prompt de sistema |
| Verification | Testar com palpites claramente errados (muito alto e muito baixo) e verificar que o tom da resposta é adequado |
| Deliverables | `guessPrice()` no ViewModel, nova Row na UI, campo com `rememberSaveable` |

### 8. Autonomous Agent Workflow

A IA foi utilizada pontualmente para:

- Definir a estrutura do prompt de sistema (`You are a professional pastry chef...`) e validar que o `trimIndent()` era necessário.
- Confirmar que `rememberSaveable` era a escolha correta para o campo de preço vs `remember`.
- Esclarecer a diferença entre lançar o pedido em `Dispatchers.IO` vs `Dispatchers.Default` para chamadas de rede.

Não houve debugging autónomo porque a funcionalidade compilou e funcionou corretamente na primeira iteração.

### 9. Verification of AI-Generated Artifacts

O código sugerido pela IA foi revisto linha a linha antes de ser integrado. Os pontos verificados foram:

- O bitmap é decodificado da mesma forma que no botão Go original, sem duplicação de lógica.
- O `_uiState.value = UiState.Loading` é definido no início do método, antes da chamada ao modelo, para que o indicador de carregamento apareça imediatamente.
- O bloco `catch` trata exceções genéricas e passa a mensagem localizada ao `UiState.Error`, consistente com o comportamento do `sendPrompt()` original.

### 10. Human vs AI Contribution

| Área | Responsável |
|---|---|
| Estrutura base do projeto (Gemini API Starter) | Android Studio (template) |
| Definição da funcionalidade extra (jogo de preços) | Humano |
| Prompt de sistema do chef de pastelaria | Humano (com apoio do Claude via Antigravity) |
| Implementação do `guessPrice()` no ViewModel | Humano (com apoio do Claude via Antigravity) |
| UI da nova Row com campo e botão | Humano |
| Testes e validação | Humano |

### 11. Ethical and Responsible Use

- A chave de API é gerida via `local.properties` e `BuildConfig`, nunca exposta no código fonte nem em commits.
- O prompt de sistema não induz o modelo a produzir conteúdo prejudicial — apenas condiciona o tom e o papel da resposta.
- O código gerado com apoio de IA foi revisto e compreendido pelo aluno antes de ser integrado.

---

## Development Process

### 12. Version Control and Commit History

O projeto partiu do template *Gemini API Starter* do Android Studio. Os commits refletem duas fases: a configuração inicial e a adição da funcionalidade extra do jogo de preços.

### 13. Difficulties and Lessons Learned

- **Template Gemini API Starter não disponível**: Ao criar um novo projeto no Android Studio, a opção *Gemini API Starter* não aparecia na lista de templates. O problema foi resolvido atualizando o Android Studio para uma versão mais recente, após o que o template ficou disponível.

- **Condicionamento do modelo via prompt**: A primeira versão do prompt para o jogo de preços era demasiado aberta e o Gemini respondia com análises longas e técnicas. Adicionar "Keep it fun and short" no final do prompt resolveu o problema — ficou claro que o modelo responde de forma significativamente diferente consoante as instruções de formato incluídas no próprio texto.

- **`rememberSaveable` vs `remember`**: O campo de preço perdia o valor ao rodar o ecrã na primeira versão porque usava `remember`. A diferença entre os dois só se tornou óbvia ao testar a rotação — `remember` mantém estado entre recomposições, mas não entre recriações da Activity; `rememberSaveable` persiste através das duas.

### 14. Future Improvements

- Adicionar suporte para o utilizador carregar a sua própria imagem da galeria ou câmara, em vez de estar limitado às três imagens incluídas.
- Implementar histórico de respostas para que o utilizador possa rever interações anteriores sem perder o contexto ao mudar de imagem.
- Explorar respostas em streaming com `generateContentStream()` para mostrar a resposta do Gemini a aparecer progressivamente, em vez de esperar pela resposta completa.

---

## 15. AI Usage Disclosure

**Código:** [AC YES, AI YES]
O desenvolvimento da funcionalidade extra foi feito com apoio do Claude (Anthropic), acedido através da aplicação Antigravity, para definição do prompt de sistema e validação de escolhas de implementação. O aluno reviu e compreende todo o código presente no projeto.

**Relatório:** [AC YES, AI YES]
A redação e estruturação deste relatório foi assistida pelo modelo Claude (Anthropic), acedido através da aplicação Antigravity. O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.

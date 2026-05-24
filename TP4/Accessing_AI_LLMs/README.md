# Assignment — AI LLMs

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 24/05/2026  
**Repository URL:** [DAM_TP4_Accessing_AI_LLMs](https://github.com/rgtd-faustino/DAM/edit/main/TP4/Accessing_AI_LLMs/)

---

## 1. Introdução

Este relatório descreve o desenvolvimento das tarefas relativas ao acesso a LLMs (Large Language Models) da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM) do ISEL, integradas no Tutorial 4 — *Flows & Firebase*.

O objetivo desta parte do trabalho foi explorar a integração de modelos de linguagem da OpenAI (GPT) e do Google (Gemini) numa aplicação Kotlin de consola, fazendo chamadas diretas às suas APIs RESTful. O projeto de base fornecido foi o `AISimpleCall`, que já continha a estrutura geral do sistema, sendo necessário compreendê-la, testá-la e extendê-la com novas funcionalidades.

As tarefas realizadas foram:

- **Task 1** — Colocar o projeto `AISimpleCall` a funcionar e familiarização com o código.
- **Task 2** — Parametrização da temperatura e dos máximos de tokens via `config.properties`.
- **Task 3** — Testes de temperatura com diferentes valores e prompts.
- **Task 4** — Implementação de análise de sentimento com resposta estruturada em JSON.

---

## 2. Visão Geral do Sistema

O projeto está organizado como uma aplicação Kotlin/Maven de consola e assenta numa arquitetura polimórfica baseada numa interface comum (`AIAssistant`) com múltiplas implementações concretas:

- `AIAssistantOpenAI` — Acede à API da OpenAI construindo o body do pedido manualmente com `JSONObject`.
- `AIAssistantGemini` — Acede à API do Google Gemini, também com `JSONObject`.
- `AIAssistantOpenAIClasses` — Versão da OpenAI que usa *data classes* serializadas com Gson.
- `AIAssistantGeminiClasses` — Versão do Gemini que usa *data classes* serializadas com Gson, incluindo suporte a `GenerationConfig`.

A criação da instância correta é delegada ao `AIAssistantFactory`, que lê a propriedade `AI_LLM` do ficheiro `config.properties` e instancia o assistente adequado. O ponto de entrada da aplicação é o `Main.kt`, que apresenta ao utilizador dois modos de operação: **chat** e **análise de sentimento**.

---

## 3. Arquitetura e Design

### Task 1 — Familiarização com o projeto base

O primeiro passo foi analisar e compreender a estrutura do projeto. A arquitetura segue o padrão *Factory + Interface*, onde a interface `AIAssistant` define o contrato comum a todos os assistentes. Os métodos mais relevantes são:

- `buildPrompt(input)` — Formata o input do utilizador num prompt com instruções de sistema.
- `buildRequest(prompt)` — Método abstrato implementado por cada subclasse, que constrói o `Request` HTTP específico para cada API.
- `makeApiCall(prompt)` — Executa a chamada HTTP e faz o *parsing* da resposta.
- `apiCallWithBackoff(input)` — Envolve a chamada com lógica de *retry* exponencial para lidar com erros 429 (rate limiting).

A divisão entre as variantes com `JSONObject` e com *data classes* permite comparar as duas abordagens de serialização. A versão com classes é mais legível e tipificada, enquanto a versão com `JSONObject` é mais explícita no que diz respeito à estrutura do JSON gerado.

### Task 2 — Temperatura e max tokens como propriedades

Para parametrizar a temperatura e o número máximo de tokens, foram adicionadas as propriedades `TEMPERATURE` e `MAX_TOKENS` ao `config.properties`. A leitura dessas propriedades foi implementada nas classes que utilizam `GenerationConfig` (Gemini) e `OpenAIRequest` (OpenAI).

Uma decisão de design relevante foi onde fazer a leitura das propriedades. Como as *data classes* de configuração (`GenerationConfig`, `OpenAIRequest`) são classes internas e não têm acesso direto ao objeto `properties` da classe envolvente, optou-se por ler os valores antes de construir os objetos e passá-los já convertidos para o construtor:

```kotlin
val temperature = properties.getProperty("TEMPERATURE").toDouble()
val maxTokens = properties.getProperty("MAX_TOKENS").toInt()

val geminiRequest = GeminiRequest(
    contents = listOf(content),
    generationConfig = GenerationConfig(temperature, maxTokens)
)
```

### Task 4 — Análise de Sentimento

A funcionalidade de análise de sentimento foi implementada diretamente na interface `AIAssistant`, como método concreto `analyzeSentiment(input)`. Desta forma, todas as implementações herdam o comportamento sem necessidade de duplicação de código.

O prompt foi construído com instruções estritas para forçar o modelo a responder **exclusivamente** com um objeto JSON, sem texto adicional fora do bloco. A escala utilizada vai de 1 a 7:

```
1 = Very Negative  ...  7 = Very Positive
```

O formato esperado na resposta é:

```json
{
    "rating": <número de 1 a 7>,
    "justification": "<breve explicação>"
}
```

No `Main.kt`, foi adicionado um menu de seleção de modo antes do ciclo principal de interação. O utilizador escolhe entre o modo **1 — Chat** e o modo **2 — Análise de Sentimento**, e essa escolha determina qual dos dois métodos (`processInput` ou `analyzeSentiment`) é invocado em cada iteração. Caso o utilizador não introduza nenhuma opção, o modo predefinido é o chat.

---

## 4. Implementação

### Estrutura de ficheiros

| Ficheiro | Tipo | Descrição |
|---|---|---|
| `AIAssistant.kt` | `interface` | Contrato comum: `buildPrompt`, `analyzeSentiment`, `apiCallWithBackoff`, `makeApiCall` |
| `AIAssistantFactory.kt` | `class` | Factory que instancia o assistente com base na propriedade `AI_LLM` |
| `AIAssistantOpenAI.kt` | `class` | Implementação OpenAI com `JSONObject` |
| `AIAssistantGemini.kt` | `class` | Implementação Gemini com `JSONObject` |
| `AIAssistantOpenAIClasses.kt` | `class` | Implementação OpenAI com *data classes* + Gson |
| `AIAssistantGeminiClasses.kt` | `class` | Implementação Gemini com *data classes* + Gson + `GenerationConfig` |
| `Main.kt` | ficheiro | Ponto de entrada com seleção de modo e ciclo de interação |
| `Utils.kt` | ficheiro | Carregamento de propriedades e configuração de logging |

### Análise de sentimento (`AIAssistant.kt`)

O prompt da análise de sentimento usa *triple-quoted strings* do Kotlin para facilitar a escrita de blocos de texto multi-linha sem necessidade de concatenações ou caracteres de escape explícitos. O `.trimIndent()` no final remove os espaços de indentação iniciais desnecessários:

```kotlin
suspend fun analyzeSentiment(input: String): String {
    val prompt = """
        Analyze the sentiment of the text below and reply only with a JSON object.
        Do not add any extra text outside the JSON.
        ...
        Text to analyze: "$input"
    """.trimIndent()

    return apiCallWithBackoff(prompt)
}
```

### Seleção de modo (`Main.kt`)

```kotlin
val mode = readlnOrNull() ?: "1"

val output: String
if (mode == "2")
    output = assistant.analyzeSentiment(input)
else
    output = assistant.processInput(input)
```

---

## 5. Testes e Validação

### Task 1 — Modelos testados

Foram testados vários modelos em ambas as APIs. A grande maioria não estava acessível com as chaves disponíveis, retornando erros de permissão ou 404. Após várias tentativas falhadas, os únicos modelos que efetivamente funcionaram foram:

- **Gemini:** `gemini-2.5-flash`
- **OpenAI:** `gpt-4o`

Todos os outros modelos comentados no código (`gemini-1.0-pro`, `gemini-1.5-flash`, `gemini-2.0-flash`, `gemini-2.5-flash-preview`, `gpt-3.5-turbo`, `gpt-4`, `o1`, `o3-mini`, etc.) foram experimentados mas não retornaram respostas válidas com as chaves de acesso utilizadas.

### Task 3 — Testes de temperatura

Temperature controls how random or creative the model's responses are. A low temperature makes the model more deterministic and predictable, always favouring the most probable next word. A high temperature spreads the probability more evenly across options, producing more varied and creative outputs.

Foram realizados testes com os valores de temperatura **0.0**, **0.5** e **1.0**, usando dois tipos de prompt:

---

#### Test Case 1: Open-ended prompt — `write a completely absurd and chaotic poem with no rules`

**Temperature 0:**
> The whisper of a purple teapot argued with a startled cloud, which promptly deflated into a puddle of forgotten Tuesdays. A monocled squirrel, wearing a tiny sombrero, tapped a rhythm on a banana peel, while the moon, made entirely of lukewarm marmalade, began to sing opera backwards. Gravity took a vacation to a dimension of fluffy socks, leaving the teacups to float aimlessly, each one humming a different, off-key lullaby. A fish on a unicycle, pedaling with existential dread, swam through a forest of spaghetti trees, whose leaves were tiny, indignant alarm clocks. The sky, a patchwork quilt of static electricity and old receipts, suddenly sneezed a rainbow of melted cheese. Everything was Tuesdays. Except for the Tuesdays that were actually Wednesdays pretending to be Tuesdays. And then a spoon spontaneously combusted into a flock of startled pigeons. The end. Or perhaps, the beginning of a very confused sandwich.

**Temperature 0.5:**
> A pickle-jar waltzed with a sentient spoon, while the moon wore a hat made of lukewarm prune. A whisper of spaghetti, a giggle of glue, the sky tasted purple, and so did the zoo. Invisible elephants tap-danced on clouds, wearing tutus of static, escaping the shrouds of a forgotten Tuesday, where teacups could sing, and a walrus played banjo on a trampoline spring. The lamppost declared itself Emperor of Lint, demanding a tribute of peppermint tint. My left sock is plotting a coup with the toast, while a rainbow-striped badger just ghosted my host. So butter your eyebrows and whistle a chair, for the chaos has spoken, and it simply doesn't care!

**Temperature 1:**
> **The Wobble of Whimsy's Whistle**
> Spoonfuls of silent starlight drip through a turnip's ear. The sky hums in purple delight, a rhinoceros sheds a small, wobbly tear. Teacups, jazz-gloved, tap-dance a jig on the back of a particularly nervous fig. Butterflies, crafted from overdue library fines, flap numerical wings over wonky chalk lines. Gravity yawns, redefines "over there," as a banana peels backwards, revealing a stare. A dandelion hums an old opera tune, then turns into marmalade, beneath the wrong moon. No rules, just the glorious, shimmering blur of a pigeon playing chess with a disgruntled purr.

**Analysis:** With an open-ended and creative prompt, the temperature effect is clearly visible. At temperature 0 the poem is more repetitive, reusing the same images (notably "Tuesdays" appears multiple times) and reads more like structured prose than chaos. At temperature 0.5 the output gains rhyme and rhythm, feeling more playful and varied. At temperature 1 the model produces the most genuinely creative result — it invents an original title, uses unexpected imagery ("butterflies crafted from overdue library fines") and feels the most unpredictable and poetic of the three.

---

#### Test Case 2: Factual prompt — `Give me 5 words to describe the color blue`

**Temperature 0:**
> 1. Calm
> 2. Deep
> 3. Vibrant
> 4. Cool
> 5. Serene

**Temperature 0.5:**
> 1. **Serene:** Evokes a sense of calm and peace, like a clear blue sky.
> 2. **Oceanic:** Brings to mind the vastness and depth of the sea.
> 3. **Cool:** Often associated with refreshing and crisp feelings, or lower temperatures.
> 4. **Vibrant:** Describes a bright, lively, and intense shade of blue.
> 5. **Mysterious:** Can suggest the dark, deep, and unknown qualities of certain blues.

**Temperature 1:**
> 1. **Melancholic:** Blue carries an emotional weight, often tied to sadness or longing.
> 2. **Infinite:** Like the sky or ocean, it stretches beyond what the eye can hold.
> 3. **Electric:** Think neon signs, lightning, the buzz of something charged and alive.
> 4. **Hushed:** A quietness lives in blue, the colour of dusk and held breath.
> 5. **Boundless:** Blue refuses edges — it bleeds into horizons and disappears.

**Analysis:** With a simple factual prompt, the temperature effect is very noticeable. At temperature 0 the model gives the most obvious and expected words with no elaboration. At temperature 0.5 it expands each word with a brief explanation, adding context but still choosing safe options. At temperature 1 the word choices become more original and poetic — "hushed" and "boundless" are far less predictable than "calm" or "cool" — showing how higher temperatures push the model toward more creative and unexpected outputs even for straightforward prompts.

---


## 6. Instruções de Utilização

### Pré-requisitos

- Java JDK 17+
- Maven 3.8+
- Kotlin 1.9+
- API Key da OpenAI e/ou Google Gemini

### Configuração

Criar o ficheiro `config.properties` na raiz do projeto com o seguinte conteúdo:

```properties
AI_LLM=GEMINI-CLASSES

OPENAI_API_KEY=<your_openai_key>
GEMINI_API_KEY=<your_gemini_key>

TEMPERATURE=0.7
MAX_TOKENS=800

LOG_LEVEL=OFF
```

### Execução

```bash
mvn compile
mvn exec:java -Dexec.mainClass="dam.MainKt"
```

---

## Autonomous Software Engineering Sections

> As secções 7 a 11 não se aplicam a este trabalho.

---

## Development Process

### 12. Version Control and Commit History

O desenvolvimento foi feito de forma incremental, começando pela Task 1 (familiarização e testes), seguida das Tasks 2 e 4 que implicaram alterações ao código base. Os commits foram organizados por tarefa para manter o histórico limpo.

### 13. Difficulties and Lessons Learned

**Ordenação dos parâmetros no data class `GenerationConfig`:**  
O problema mais inesperado ocorreu na classe `AIAssistantGeminiClasses`. Ao construir o objeto `GenerationConfig(temperature, maxTokens)`, os valores não estavam a ficar nos atributos corretos. O motivo foi a ordem de declaração dos parâmetros na *data class*: a `temperature` estava declarada após `maxOutputTokens`, pelo que o segundo argumento passado ia para `topK` em vez de para `maxOutputTokens`. A solução foi reordenar a declaração dos campos na *data class* para corresponder à ordem de uso esperada no construtor.

**Leitura de propriedades dentro de data classes:**  
Numa tentativa inicial, tentou-se ler os valores de `properties` diretamente nos valores por defeito dos parâmetros da `GenerationConfig`. Isso não é possível porque a *data class* é uma classe separada e não tem acesso à instância de `properties` da classe envolvente. A solução foi ler os valores no método `buildRequest()` e passá-los já convertidos ao construtor.

**Compatibilidade de modelos:**  
Foi necessário testar empiricamente um grande número de modelos para encontrar os que funcionavam com as chaves de acesso disponíveis. A maioria retornava erros 404 ou de permissão, sem qualquer indicação clara de qual seria compatível. No final, o único modelo Gemini que funcionou foi o `gemini-2.5-flash` e o único da OpenAI foi o `gpt-4o`.

**Forçar resposta JSON no modelo:**  
Na análise de sentimento, nas primeiras tentativas, o modelo incluía texto explicativo antes ou depois do bloco JSON, como "Here is the analysis:" ou "Let me know if you need anything else.", o que quebrava o *parsing* posterior. A solução foi reforçar as instruções no prompt, adicionando explicitamente `"Do not add any extra text outside the JSON"`.

### 14. Future Improvements

- **Parsing do JSON de sentimento:** Implementar o *parsing* da resposta JSON diretamente na aplicação, de forma a apresentar o rating e a justificação de forma formatada em vez de mostrar o JSON em bruto.
- **Histórico de conversa:** Guardar o histórico das mensagens e enviá-lo em cada chamada para que o modelo tenha contexto das trocas anteriores, tornando o modo chat verdadeiramente conversacional.
- **Suporte a temperatura nas variantes com `JSONObject`:** Atualmente, a parametrização de temperatura via `config.properties` só está implementada nas variantes com *data classes*. As variantes `OPENAI` e `GEMINI` (com `JSONObject`) não leem ainda esses valores.
- **Validação de propriedades em falta:** Adicionar tratamento de erros explícito para o caso de `TEMPERATURE` ou `MAX_TOKENS` não estarem definidos no `config.properties`, com fallback para valores por defeito documentados.

### 15. AI Usage Disclosure

**Código:** `[AC YES, AI NO]`  
Todo o código foi desenvolvido pelo aluno com recurso a autocomplete do IDE (IntelliJ), mas sem ferramentas de geração de código por inteligência artificial. Todo o código e comentários foram escritos manualmente.

**Relatório:** `[AC YES, AI YES]`  
A redação e estruturação deste relatório foram assistidas pelo modelo Claude (Anthropic). O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.

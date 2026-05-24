# Assignment — Kotlin Flows & Coroutines

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 24/05/2026  
**Repository URL:** DAM_TP4

---

## 1. Introdução

Este relatório descreve o desenvolvimento da Secção 2 do Tutorial 4 da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM) do ISEL, dedicada a Kotlin Flows e Coroutines. O objetivo principal foi compreender e aplicar os mecanismos de programação assíncrona disponíveis em Kotlin — desde o modelo de bloqueio mais simples até à gestão de estados reativos com `StateFlow` — numa aplicação Swing que consome a API do GitHub.

O trabalho está dividido em três passos: o primeiro consiste em seguir o tutorial oficial da Kotlin sobre coroutines e channels, transformando progressivamente uma aplicação bloqueante numa solução concorrente e reativa; o segundo e terceiro passos são exercícios DIY que estendem o projeto com uma gestão de estados de carregamento mais robusta, usando `StateFlow` e um canal adicional de progresso para desacoplar o produtor de dados da atualização da UI.

---

## 2. Visão Geral do Sistema

O projeto `intro-coroutines` é uma aplicação desktop com interface Java Swing que carrega dados de contribuidores de repositórios de uma organização no GitHub. A lógica de carregamento foi progressivamente refatorada ao longo de oito variantes distintas, selecionáveis via dropdown na UI:

- **BLOCKING** — carregamento síncrono na main thread; congela a UI.
- **BACKGROUND** — carregamento numa thread separada via callback.
- **CALLBACKS** — pedidos assíncronos com a API `Call.enqueue()` do Retrofit.
- **SUSPEND** — funções `suspend` com coroutines; código limpo sem callbacks.
- **CONCURRENT** — pedidos em paralelo com `async`/`awaitAll()`.
- **NOT_CANCELLABLE** — demonstração de coroutines não canceláveis com `GlobalScope`.
- **PROGRESS** — atualização progressiva da UI a cada repo carregado.
- **CHANNELS** — concorrência + progresso usando `Channel` como fila de mensagens.

O Step 2 e Step 3 introduziram ainda uma gestão de estado reativa com `StateFlow` em substituição de chamadas diretas ao método `setLoadingStatus`, e um canal de progresso (`progressChannel`) na variante CHANNELS para desacoplar a produção de dados da atualização visual.

---

## 3. Arquitetura e Design

### Step 1 — Tutorial Oficial

#### Task 1 — Aggregation

A função `List<User>.aggregate()` foi implementada como uma extension function que encadeia três operações:

```kotlin
fun List<User>.aggregate(): List<User> =
    groupBy { it.login }
        .map { (login, group) -> User(login, group.sumOf { it.contributions }) }
        .sortedByDescending { it.contributions }
```

O `groupBy` agrupa utilizadores repetidos (um por repositório) pelo `login`; o `map` cria um único `User` por grupo com a soma das contribuições; e o `sortedByDescending` ordena do maior para o menor.

#### Task 2 — Background Thread

O problema do código original era que o resultado do `loadContributorsBlocking` era calculado mas descartado — nunca chegava ao callback:

```kotlin
// antes: resultado perdido
thread { loadContributorsBlocking(service, req) }

// depois: resultado passado ao callback
thread {
    val users = loadContributorsBlocking(service, req)
    updateResults(users)
}
```

O `SwingUtilities.invokeLater` do lado do chamador garante que a atualização da UI ocorre na main thread, respeitando as restrições do Swing.

#### Task 3 — Callbacks

O problema nesta variante era de timing: os pedidos de contributors para cada repo eram assíncronos, mas o `updateResults` era chamado imediatamente a seguir ao loop, antes de qualquer resposta ter chegado. A solução passou por um contador decrescente:

```kotlin
var countDown = repos.size
for (repo in repos) {
    service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
        allUsers += users
        countDown--
        if (countDown == 0) {
            updateResults(allUsers.aggregate())
        }
    }
}
```

O `updateResults` só é chamado quando o último callback responde, garantindo que `allUsers` está completo.

#### Task 4 — Suspend Functions

A versão `suspend` é estruturalmente idêntica à `BLOCKING`, com duas diferenças: não existe chamada a `.execute()`, e usam-se as funções `suspend` declaradas na interface `GitHubService`. O Retrofit trata internamente da suspensão da coroutine durante a espera pela resposta HTTP, sem bloquear a thread.

```kotlin
suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    val repos = service.getOrgRepos(req.org).also { logRepos(req, it) }.bodyList()
    return repos.flatMap { repo ->
        service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
    }.aggregate()
}
```

#### Task 5 — Concorrência

A variante CONCURRENT dispara um `async` por cada repo, em paralelo, em vez de esperar pela resposta de cada um antes de pedir o próximo:

```kotlin
val deferreds = repos.map { repo ->
    async(Dispatchers.Default) {
        log("starting loading for ${repo.name}")
        delay(3000)
        service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
    }
}
deferreds.awaitAll().flatten().aggregate()
```

O `delay(3000)` foi introduzido propositadamente para criar uma janela de tempo que permita testar o cancelamento antes dos pedidos HTTP serem efetivamente enviados.

#### Not Cancellable

Esta variante demonstra o efeito de usar `GlobalScope.async` em vez de `coroutineScope`. As coroutines criadas com `GlobalScope` são independentes da coroutine pai — se o utilizador cancelar o carregamento na UI, os pedidos HTTP continuam em background porque não existe relação hierárquica entre pai e filhos. Com `coroutineScope`, o cancelamento propaga-se automaticamente.

#### Progress

A variante PROGRESS acumula os utilizadores progressivamente, atualizando a UI após cada repo:

```kotlin
var allUsers = emptyList<User>()
for ((index, repo) in repos.withIndex()) {
    val users = service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
    allUsers = (allUsers + users).aggregate()
    updateResults(allUsers, index == repos.lastIndex)
}
```

A desvantagem é que o carregamento continua a ser sequencial — o repo seguinte só é pedido depois de receber a resposta do anterior.

#### Channels

A variante CHANNELS combina as vantagens do CONCURRENT (pedidos em paralelo) com as do PROGRESS (atualização incremental da UI). Um `Channel<List<User>>` funciona como fila de mensagens: cada coroutine de repo envia os seus resultados assim que termina, e o consumidor processa-os pela ordem de chegada, sem esperar por todos:

```kotlin
val channel = Channel<List<User>>()
for (repo in repos) {
    launch {
        val users = service.getRepoContributors(req.org, repo.name).also { logUsers(repo, it) }.bodyList()
        channel.send(users)
    }
}
var allUsers = emptyList<User>()
repeat(repos.size) {
    val users = channel.receive()
    allUsers = (allUsers + users).aggregate()
    updateResults(allUsers, it == repos.lastIndex)
}
```

O `receive()` suspende a coroutine enquanto o canal está vazio, libertando a thread sem desperdício de recursos.

---

### Step 2 — StateFlow para Gestão de Estado

**Task 1 — LoadingStateData**

O enum `LoadingStatus` foi complementado com um estado `INIT` (para mostrar uma mensagem inicial antes de qualquer pedido) e com uma data class `LoadingStateData` que agrega o estado, o timestamp de início e o tempo decorrido:

```kotlin
enum class LoadingStatus { INIT, COMPLETED, CANCELED, IN_PROGRESS }

data class LoadingStateData(
    val status: LoadingStatus = LoadingStatus.INIT,
    val startTime: Long? = null,
    val elapsedTime: String = ""
)
```

**Task 2 — Backing Property Pattern**

A interface `Contributors` expõe um `StateFlow<LoadingStateData>` imutável. A implementação em `ContributorsUI` usa o padrão backing property: um `MutableStateFlow` privado (`_loadingState`) que só esta classe pode modificar, exposto publicamente como `StateFlow` somente de leitura:

```kotlin
private val _loadingState = MutableStateFlow(Contributors.LoadingStateData())
override val loadingState: StateFlow<Contributors.LoadingStateData> = _loadingState.asStateFlow()
```

**Task 3 — Coletor Reativo**

O método `observeLoadingStatus()` lança uma coroutine que coleta o flow e atualiza os componentes Swing sempre que o estado muda, em vez de chamadas diretas a `setLoadingStatus`. O método `updateLoadingStatus()` apenas emite um novo valor para o `MutableStateFlow`:

```kotlin
override fun updateLoadingStatus(newStatus: Contributors.LoadingStateData) {
    _loadingState.value = newStatus
}
```

A remoção de `setLoadingStatus` da interface é uma consequência direta desta abordagem — a UI já não precisa de ser informada explicitamente; reage automaticamente às emissões do flow.

---

### Step 3 — Canal de Progresso na Variante CHANNELS

O código original da variante CHANNELS chamava `updateResults` diretamente no callback de `loadContributorsChannels`. O Step 3 introduz um `progressChannel` buffered como camada intermédia entre o produtor (carregamento de dados) e o consumidor (atualização da UI), o que traz controlo de back-pressure e separação clara de responsabilidades:

```kotlin
val progressChannel = Channel<Pair<List<User>, Boolean>>(Channel.BUFFERED)

launch(Dispatchers.Default) {
    loadContributorsChannels(service, req) { users, completed ->
        progressChannel.send(Pair(users, completed))
    }
    progressChannel.close()
}

for ((users, completed) in progressChannel) {
    withContext(Dispatchers.Main) {
        updateResults(users, startTime, completed)
    }
}
```

O canal é fechado explicitamente após o carregamento terminar, o que provoca o fim automático do `for` loop no consumidor. O `Channel.BUFFERED` permite que o produtor continue a enviar dados mesmo que o consumidor ainda esteja a processar o update anterior.

---

## 4. Implementação

| Ficheiro | Variante | Descrição |
|---|---|---|
| `Aggregation.kt` | — | Extension function `aggregate()` |
| `Request1Blocking.kt` | BLOCKING | Base de comparação; bloqueia a main thread |
| `Request2Background.kt` | BACKGROUND | Callback com thread separada |
| `Request3Callbacks.kt` | CALLBACKS | Retrofit `enqueue()` com countdown |
| `Request4Suspend.kt` | SUSPEND | Funções `suspend` sem `.execute()` |
| `Request5Concurrent.kt` | CONCURRENT | `async`/`awaitAll()` com `delay` para demo de cancelamento |
| `Request5NotCancellable.kt` | NOT_CANCELLABLE | `GlobalScope.async` sem hierarquia |
| `Request6Progress.kt` | PROGRESS | Atualização incremental sequencial |
| `Request7Channels.kt` | CHANNELS | Canal como fila de mensagens concorrente |
| `Contributors.kt` | — | Interface com `StateFlow`, `LoadingStateData`, lógica de cancelamento |
| `ContributorsUI.kt` | — | Backing property pattern, `observeLoadingStatus()` |

---

## 5. Testes e Validação

Os testes foram realizados manualmente através da execução da aplicação Swing com cada variante selecionada no dropdown, usando a organização `kotlin` do GitHub. Foram validados os seguintes cenários:

- **BLOCKING**: confirmado que a UI congela durante o carregamento.
- **BACKGROUND**: confirmado que a UI permanece responsiva e os resultados aparecem após conclusão.
- **CALLBACKS**: confirmado que os resultados aparecem, e que o código comentado (sem contador) não produz resultados — a lista estava vazia no momento da chamada.
- **SUSPEND**: confirmado nos logs que todos os pedidos correm na main thread sem a bloquear.
- **CONCURRENT**: confirmado nos logs que os pedidos são disparados em paralelo; testado o cancelamento durante o `delay(3000)` — os logs param imediatamente.
- **NOT_CANCELLABLE**: confirmado que após cancelar na UI os logs continuam a aparecer, porque as coroutines do `GlobalScope` não são filhas do job cancelado.
- **PROGRESS**: confirmado que a lista na UI vai crescendo a cada repo carregado.
- **CHANNELS**: confirmado que os resultados chegam pela ordem de resposta (não pela ordem dos repos) e que a UI atualiza progressivamente.
- **StateFlow (Step 2)**: confirmado que o estado `init` aparece no arranque, e que as transições `in progress` → `completed`/`canceled` funcionam corretamente sem chamar `setLoadingStatus` diretamente.
- **progressChannel (Step 3)**: confirmado que o canal é fechado após o carregamento, terminando o loop do consumidor.

---

## 6. Instruções de Utilização

**Pré-requisitos**

- IntelliJ IDEA
- JDK 17 ou superior
- Token de acesso à API do GitHub (scope vazio é suficiente)

**Setup**

Clonar o repositório e abrir no IntelliJ:

```
git clone https://github.com/kotlin-hands-on/intro-coroutines
```

Colocar o token GitHub no ficheiro `resources/github.properties`:

```
githubToken=<o_teu_token>
```

Sincronizar as dependências Gradle (botão de elefante no IntelliJ → *Reload All Gradle Projects*) e correr o `fun main()` em `src/contributors/main.kt`.

**Estrutura relevante do projeto**

```
src/
├── contributors/
│   ├── main.kt
│   ├── Contributors.kt       # Interface principal + StateFlow
│   ├── ContributorsUI.kt     # UI Swing + backing property pattern
│   ├── GitHubService.kt      # Interface Retrofit com funções suspend
│   └── Logger.kt
└── tasks/
    ├── Aggregation.kt
    ├── Request1Blocking.kt
    ├── Request2Background.kt
    ├── Request3Callbacks.kt
    ├── Request4Suspend.kt
    ├── Request5Concurrent.kt
    ├── Request5NotCancellable.kt
    ├── Request6Progress.kt
    └── Request7Channels.kt
```

---

## Autonomous Software Engineering Sections

As secções 7 a 11 não se aplicam a este trabalho. O código das variantes de coroutines e channels foi desenvolvido com **[AC NO, AI NO]**. O código do Step 2 e Step 3 (StateFlow e progressChannel) foi igualmente desenvolvido com **[AC NO, AI NO]**. A IA foi utilizada apenas na redação deste relatório (ver secção 15).

---

## 12. Version Control and Commit History

Os commits foram feitos de forma incremental, por tarefa, refletindo a progressão natural do tutorial. Cada commit corresponde tipicamente à conclusão de uma variante ou de um passo DIY.

---

## 13. Dificuldades e Lições Aprendidas

- **Distinção entre suspend function e coroutine**: No início houve confusão entre os dois conceitos. A clarificação foi que uma suspend function é o que pode ser pausado, e a coroutine é o contexto necessário para que essa pausa aconteça — não são alternativas, são complementares.

- **Problema de timing nos Callbacks**: Ao olhar para o código original da Task 3, a primeira reação foi "não está já bom?" — o loop estava lá, os pedidos eram feitos, a lista existia. O problema não era visível porque o `updateResults` era chamado logo a seguir ao loop, quando `allUsers` ainda estava vazio — os callbacks ainda não tinham respondido. A correção foi mover o `updateResults` para dentro do callback, chamando-o apenas quando o contador chegasse a zero.

- **CONCURRENT vs NOT_CANCELLABLE sem diferença visível**: Ao testar as duas variantes e clicar Cancel, não se notou qualquer diferença entre elas. A razão era que o botão estava a ser clicado tarde demais — depois do `delay(3000)` já ter expirado e os pedidos HTTP terem sido enviados. A diferença real manifesta-se nos logs durante a janela do delay: no CONCURRENT os logs param; no NOT_CANCELLABLE continuam mesmo depois de cancelar, porque as coroutines do `GlobalScope` não têm o job da UI como pai.

- **PROGRESS vs CHANNELS parecem fazer a mesma coisa**: A dúvida surgiu porque ambas as variantes atualizam a UI a cada repo carregado. A diferença é que o PROGRESS é sequencial — só pede o repo seguinte depois de receber a resposta do anterior — enquanto o CHANNELS dispara todos os pedidos em paralelo e processa os resultados pela ordem em que chegam. O resultado visual é semelhante, mas o CHANNELS é mais rápido porque não há tempo de espera entre pedidos.

- **Suspend functions sem `.execute()` — como é que funciona?**: Ao implementar a Task 4, a questão foi como é que o Retrofit faz o pedido HTTP se não se chama `.execute()`. A resposta é que quando o Retrofit deteta que a função na interface está marcada como `suspend`, usa internamente um mecanismo de callback para suspender a coroutine durante a espera pela resposta, em vez de bloquear a thread. O `.execute()` deixa de ser necessário porque o próprio compilador Kotlin, em conjunto com o Retrofit, trata disso por baixo.

---

## 14. Melhorias Futuras

- Substituir o `Channel` em `Request7Channels.kt` por um `Flow` com `channelFlow` ou `flowOf`, que é a abordagem mais idiomática e moderna em Kotlin para este padrão.
- Adicionar tratamento de erros explícito nas variantes com coroutines — atualmente um erro de rede faz com que a exceção se propague silenciosamente.
- Migrar a interface de Java Swing para Compose Desktop, que se integra nativamente com coroutines e StateFlow sem necessitar de `SwingUtilities.invokeLater`.

---

## 15. AI Usage Disclosure

**Código:** [AC NO, AI NO]  
Todo o código foi desenvolvido inteiramente pelo aluno sem recurso a ferramentas de autocomplete assistido por IA nem a ferramentas de geração de código por inteligência artificial.

**Relatório:** [AC YES, AI YES]  
A redação e estruturação deste relatório foi assistida pelo modelo Claude (Anthropic). O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.
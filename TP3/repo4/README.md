# Assignment 3 — MIP: My Cat Gallery App (Multi-módulo)

**Course:** Desenvolvimento de Aplicações Móveis (DAM)
**Student:** A51394 Rafael Faustino
**Date:** 03/05/2026
**Repository URL:** [DAM_TP3_MIP](https://github.com/rgtd-faustino/DAM/tree/main/TP3/repo4)

---

## High-Level Project Description

A **My Cat Gallery App** é uma aplicação Android desenvolvida no âmbito da secção MIP-3 da unidade curricular de DAM. O projeto parte da versão anterior da galeria de gatos e evolui para uma arquitetura multi-módulo, onde a lógica de negócio e o acesso a dados ficam isolados num módulo partilhado (`:core`), consumido por dois módulos de interface distintos: um baseado em XML Views (`:app`) e outro construído exclusivamente em Jetpack Compose (`:app-compose`).

### Objetivo do Projeto

O objetivo principal é demonstrar a separação rigorosa entre camadas de dados e camadas de interface, garantindo que ambas as UIs partilham o mesmo núcleo funcional sem duplicação de código. Em termos de funcionalidades, a aplicação mantém tudo o que foi construído anteriormente: galeria de imagens aleatórias, detalhes de cada gato com informação de raça, lista de favoritos com lógica FIFO e suporte a cache offline.

### API Utilizada

A aplicação consome a **[TheCatAPI](https://thecatapi.com/)**, que fornece imagens de gatos em alta resolução e metadados sobre raças. A autenticação é feita por cabeçalho `x-api-key` em cada pedido, com a chave configurada de forma segura via `local.properties` e `BuildConfig`.

---

## 1. Introdução

Este relatório descreve o desenvolvimento do exercício MIP-3 da unidade curricular de DAM, que consistiu em refatorar e expandir a My Cat Gallery App para uma arquitetura multi-módulo. A aplicação foi reorganizada em três módulos com responsabilidades separadas: `:core` para dados e lógica, `:app` para a interface em XML e `:app-compose` para a interface em Jetpack Compose.

O módulo `:app-compose` foi desenvolvido de raiz com funcionalidades exclusivas do Compose: animações com `AnimatedVisibility` e `animateContentSize`, tema claro e escuro com alternância em tempo real, pull-to-refresh na galeria, pinch-to-zoom no ecrã de detalhes e layout adaptativo com `LazyVerticalGrid`.

---

## 2. Visão Geral do Sistema

O projeto está organizado em três módulos:

- **`:core`** -- contém os modelos de dados (`CatImage`, `Breed`), o cliente de API (`CatApiService` com Retrofit), o repositório (`CatRepository`) e os gestores de persistência local (`CacheManager`, `FavoritesManager`). Não tem qualquer dependência de UI.
- **`:app`** -- interface em XML com `GalleryFragment`, `FavoritesFragment` e `ImageDetailActivity`. Usa Glide para carregar imagens, `RecyclerView` com adaptadores e `BottomNavigationView` para navegação entre separadores. Os ViewModels usam `LiveData`.
- **`:app-compose`** -- interface em Jetpack Compose com `GalleryScreen`, `FavoritesScreen` e `DetailScreen`. Usa Coil para carregar imagens, `NavHost` para navegação e `StateFlow` nos ViewModels. Inclui funcionalidades exclusivas que não existem no módulo `:app`.

Ambos os módulos de UI dependem do `:core` e acedem aos dados exclusivamente através do `CatRepository`, que decide de forma transparente se serve os dados da rede ou da cache local.

---

## 3. Arquitetura e Design

### Multi-módulo e separação de responsabilidades

A refatoração para multi-módulo obrigou a isolar tudo o que não é interface no `:core`. O `CatRepository` passou a ser a única fonte de verdade para os dados, implementando a estratégia de fallback: tenta a rede e, em caso de falha, serve os dados da cache. O `FavoritesManager` e o `CacheManager` ficaram também no `:core`, garantindo que a lógica FIFO e os limites de 5 favoritos e 50 imagens em cache são aplicados da mesma forma em ambas as interfaces.

### Módulo `:app` (XML)

O módulo XML mantém a estrutura de Fragments com `BottomNavigationView`. Os ViewModels usam `LiveData` e expõem os estados de carregamento, erro e dados. A navegação para o ecrã de detalhes é feita via `Intent` com extras.

### Módulo `:app-compose` (Jetpack Compose)

O módulo Compose foi construído de raiz, com ViewModels que expõem `StateFlow` e composables que observam o estado com `collectAsStateWithLifecycle()`. A navegação é gerida por `NavHost` com rotas tipadas. As funcionalidades exclusivas do Compose implementadas foram:

- **`AnimatedVisibility` com `fadeIn`/`fadeOut`** nos cards da galeria durante o carregamento.
- **`animateContentSize`** no card de raça do ecrã de detalhes, que expande e colapsa ao toque.
- **Light/Dark mode** com botão de alternância na `TopAppBar`, aplicado em tempo real via `MaterialTheme`.
- **Pull-to-refresh** com `PullToRefreshBox` do Material 3 na galeria.
- **Pinch-to-zoom** na imagem do ecrã de detalhes com `detectTransformGestures`.
- **`LazyVerticalGrid` adaptativo** com 2 colunas em portrait e 3 em landscape.

---

## 4. Implementação

### Módulo `:core`

O `CatRepository` é inicializado com um `Context` para aceder aos gestores de persistência. O `CatApiService` é uma interface Retrofit com dois endpoints: pesquisa de imagens com `has_breeds=1` e detalhe individual por ID. O `FavoritesManager` e o `CacheManager` usam `SharedPreferences` com serialização Gson.

### Módulo `:app`

Os adaptadores `CatImageAdapter` e `FavoritesAdapter` usam `notifyDataSetChanged()` por simplicidade. Os fragmentos obtêm os ViewModels com `ViewModelProvider` e observam a `LiveData` para atualizar a interface. O `GalleryFragment` partilha o `MainViewModel` com a `Activity` para que os dados sobrevivam à navegação entre separadores.

### Módulo `:app-compose`

A `MainActivity` cria os três ViewModels com `by viewModels()` e passa-os ao `MainScreen`. O `NavHost` gere as rotas `gallery`, `favorites` e `detail/{imageId}`. A `TopAppBar` e a `NavigationBar` só aparecem nas rotas principais, desaparecendo no ecrã de detalhes. O `DetailScreen` usa `LaunchedEffect(imageId)` para disparar o pedido de detalhes à API quando o ecrã é aberto.

---

## 5. Testes e Validação

A validação foi feita manualmente no emulador Android.

- **Galeria e atualização**: confirmado que ao puxar para baixo (pull-to-refresh) ou ao abrir a aplicação as imagens são carregadas corretamente.
- **Ecrã de detalhes**: verificado que as dimensões reais, o ID, o URL e a informação de raça aparecem corretamente após a chamada ao endpoint de detalhe.
- **Favoritos FIFO**: testado adicionar o 6.º favorito e confirmar que o mais antigo é removido automaticamente.
- **Cache offline**: simulado modo avião e verificado que a aplicação apresenta imagens da cache com mensagem de erro adequada.
- **Animações e gestos**: verificado o fadeIn dos cards, a expansão do card de raça, o pinch-to-zoom e o pull-to-refresh no módulo Compose.
- **Tema**: confirmada a alternância entre tema claro e escuro sem reiniciar a aplicação.

---

## 6. Instruções de Utilização

### Pré-requisitos

- Android Studio Hedgehog ou superior
- Android SDK API 24+
- Uma chave de API da [TheCatAPI](https://thecatapi.com/signup)

### Configuração

1. Clonar o repositório.
2. No ficheiro `local.properties` na raiz do projeto, adicionar:
   `CAT_API_KEY=a_tua_chave_aqui`
3. Sincronizar o projeto com o Gradle.

### Execução

Selecionar o módulo pretendido (`:app` ou `:app-compose`) no Android Studio e correr no emulador ou dispositivo com o botão Run.

### Estrutura de módulos

```
MyCatGalleryApp/
├── core/
│   ├── api/
│   │   └── CatApiService.kt
│   ├── data/
│   │   ├── CacheManager.kt
│   │   └── FavoritesManager.kt
│   ├── model/
│   │   └── CatImage.kt
│   └── repository/
│       └── CatRepository.kt
├── app/
│   ├── adapter/
│   ├── ui/
│   └── viewmodel/
└── app-compose/
    ├── ui/
    │   ├── screens/
    │   └── theme/
    └── viewmodel/
```

---

# Autonomous Software Engineering Sections

## 7. Prompting Strategy

O desenvolvimento utilizou o agente Gemini Pro fornecido pela aplicação Antigravity seguindo uma abordagem de planeamento primeiro. Antes de qualquer geração de código, foram criados os ficheiros de documentação em `/docs` com a arquitetura, o modelo de dados, os ecrãs, a navegação e o plano de implementação. O ficheiro `agents.md` definiu as regras que o agente devia seguir em cada módulo.

| Componente | Descrição |
|---|---|
| Context | Aplicação multi-módulo Android com `:core`, `:app` e `:app-compose` |
| Goal | Refatorar a galeria de gatos para arquitetura multi-módulo com duas UIs distintas |
| Constraints | MVVM, Retrofit, Glide/Coil, SharedPreferences/Gson, FIFO, API 24+, XML no `:app`, Compose no `:app-compose` |
| Plan | Seguimento do `08_implementation_plan.md` passo a passo por módulo |
| Verification | Compilação após cada passo, testes no emulador |
| Deliverables | Código Kotlin, layouts, documentação e três ficheiros de entrega finais |

---

## 8. Autonomous Agent Workflow

O agente Antigravity foi usado ao longo de 38 prompts distribuídos pelas três fases do projeto: módulo `:core`, módulo `:app` e módulo `:app-compose`.

**Planeamento:**
O aluno criou os ficheiros de documentação antes de começar a gerar código. O agente foi instruído a ler sempre esses ficheiros antes de gerar qualquer ficheiro.

**Implementação:**
O agente geriu a criação dos três módulos de forma incremental, seguindo o plano passo a passo. O módulo `:core` foi implementado primeiro para garantir que ambas as UIs podiam depender dele desde o início.

**Debugging autónomo:**
O agente identificou e corrigiu os seguintes problemas:

| Problema | Solução |
|---|---|
| Ícones `Brightness4` e `Brightness7` inexistentes no Compose | Substituição por `Icons.Default.Settings` e `Icons.Default.CheckCircle` |
| `isSystemInDarkTheme()` chamado fora de contexto `@Composable` | Movido para fora do bloco `remember` |
| Conflitos de ambiente com `jlink` | Execução de `clean` e ajuste do JDK |

**Intervenção humana:**
- Definição da arquitetura multi-módulo e das regras do `agents.md`.
- Criação de toda a documentação em `/docs` antes de gerar código.
- Decisão das funcionalidades exclusivas do módulo Compose.
- Aprovação de cada passo antes de avançar para o seguinte.
- Reporte dos erros de compilação ao agente com contexto suficiente para os resolver.

---

## 9. Verification of AI-Generated Artifacts

O código gerado foi verificado através de:

- **Compilação incremental**: cada módulo foi compilado após cada passo do plano para detetar erros cedo.
- **Testes funcionais**: validação manual de todas as funcionalidades em emulador Android.
- **Revisão dos deliverables**: os três ficheiros finais (`10_module_diagram.md`, `11_ui_contract.md`, `12_refactoring_plan.md`) foram revistos pelo aluno para garantir que refletem com rigor a arquitetura implementada.

---

## 10. Human vs AI Contribution

| Área | Responsável |
|---|---|
| Documentação de planeamento (`/docs`) | Humano |
| Regras do agente (`agents.md`) | Humano |
| Módulo `:core` (modelos, API, repositório, cache, favoritos) | IA (Antigravity) |
| Módulo `:app` (fragments, adapters, viewmodels XML) | IA (Antigravity) |
| Módulo `:app-compose` (screens, viewmodels, navegação, tema) | IA (Antigravity) |
| Correção de erros de compilação | IA (com reporte humano) |
| Testes e validação | Humano |
| Deliverables finais (módulo diagram, UI contract, refactoring plan) | IA (com revisão humana) |
| Elaboração do relatório | Humano (com assistência da IA) |

---

## 11. Ethical and Responsible Use

O uso de IA neste projeto seguiu as regras definidas no enunciado e no `agents.md`. Os principais cuidados foram:

- **Planeamento antes do código**: o agente só gerou código depois de toda a documentação estar definida, o que reduziu inconsistências e retrabalho.
- **Validação funcional**: cada ficheiro gerado foi compilado e testado antes de avançar para o passo seguinte.
- **Segurança**: a chave de API nunca foi incluída em ficheiros de código, ficando sempre isolada no `local.properties`.

---

# Development Process

## 12. Version Control and Commit History

O projeto foi desenvolvido de forma incremental, com commits por módulo. A ordem de implementação foi: `:core` primeiro, depois `:app`, e por fim `:app-compose`, o que garantiu que a base estava estável antes de construir as interfaces em cima dela.

---

## 13. Difficulties and Lessons Learned

A principal dificuldade encontrada foi que, na primeira versão do ecrã de detalhes, nem toda a informação disponível sobre o gato aparecia logo de início. Ao abrir o ecrã, apenas parte dos dados era mostrada, e a informação completa de raça, dimensões e ID só ficava visível depois de clicar na imagem ou interagir com o ecrã. O problema estava na ordem em que os dados eram carregados e no momento em que os composables eram desenhados em relação ao `StateFlow`. A solução passou por garantir que o `LaunchedEffect` disparava o pedido à API imediatamente ao abrir o ecrã e que todos os campos do card de informação aguardavam os dados antes de tentar mostrá-los.

---

## 14. Future Improvements

- **Persistência com Room**: substituir o `SharedPreferences` no `FavoritesManager` por uma base de dados Room para suportar mais metadados e pesquisa por raça.
- **Paginação**: implementar `Paging 3` para carregar imagens de forma mais eficiente em lotes, em vez de um número fixo por pedido.
- **Testes automatizados**: adicionar testes unitários ao `CatRepository` e testes de composable com `ComposeTestRule`.

---

## 15. AI Usage Disclosure

**Código: [AC YES, AI YES]**
Este projeto foi desenvolvido com assistência extensiva da IA Gemini Pro fornecida pela aplicação Antigravity. O aluno orientou o desenvolvimento através de 38 prompts estruturados (no total), revendo cada passo e validando funcionalmente o código gerado.

**Relatório: [AC YES, AI YES]**
A redação e estruturação deste relatório foi assistida pelo modelo **Claude (Anthropic)**. O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.
# Registo de Prompts

## Prompt 1
Objetivo: Adicionar dependências ao build.gradle
Prompt utilizado: Lê os ficheiros agents.md e docs/08_implementation_plan.md e executa o Passo 1: adiciona as dependências Retrofit, Glide, ViewModel e LiveData ao build.gradle.
Resultado: Dependências adicionadas ao libs.versions.toml e ao build.gradle.kts usando Version Catalogs.

## Prompt 2
Objetivo: Adicionar permissão de Internet
Prompt utilizado: Executa o Passo 2: adiciona a permissão de Internet ao AndroidManifest.xml.
Resultado: Permissão INTERNET adicionada ao AndroidManifest.xml.

## Prompt 3
Objetivo: Criar modelo de dados
Prompt utilizado: Executa o Passo 3: cria a classe de modelo de dados CatImage.kt.
Resultado: Classe CatImage.kt criada no pacote model com os campos id, url, width, height e isFavourite usando @SerializedName do Gson.

## Prompt 4
Objetivo: Criar serviço de API com Retrofit
Prompt utilizado: Executa o Passo 4: cria a interface do serviço de API CatApiService.kt usando Retrofit.
Resultado: Interface CatApiService.kt criada com Retrofit usando suspend fun e coroutines.

## Prompt 5
Objetivo: Criar o Repositório
Prompt utilizado: Executa o Passo 5: cria a classe CatRepository.kt.
Resultado: Classe CatRepository.kt criada com inicialização do Retrofit e método getCatImages.

## Prompt 6
Objetivo: Criar o ViewModel
Prompt utilizado: Executa o Passo 6: cria o ViewModel MainViewModel.kt com LiveData<List<CatImage>>.
Resultado: MainViewModel.kt criado no pacote viewmodel com LiveData para lista de imagens, estado de loading e mensagens de erro.

## Prompt 7
Objetivo: Desenhar o layout activity_main.xml
Prompt utilizado: Executa o Passo 7: desenha o layout activity_main.xml com RecyclerView, botão de atualização e ProgressBar.
Resultado: Layout activity_main.xml criado com RecyclerView, ProgressBar e Button de atualização usando ConstraintLayout.

## Prompt 8
Objetivo: Criar o adaptador da RecyclerView
Prompt utilizado: Executa o Passo 8: cria o adaptador CatImageAdapter.kt usando Glide para carregar as imagens.
Resultado: CatImageAdapter.kt criado com Glide para carregar imagens e item_cat.xml com CardView e ImageView.

## Prompt 9
Objetivo: Ligar MainActivity ao ViewModel
Prompt utilizado: Executa o Passo 9: liga a MainActivity.kt ao ViewModel e observa a LiveData para atualizar a interface.
Resultado: MainActivity.kt ligada ao ViewModel com observers para catImages, isLoading e errorMessage.

## Prompt 10
Objetivo: Criar ecrã de detalhes
Prompt utilizado: Executa o Passo 10: cria o layout activity_detail.xml e a classe ImageDetailActivity.kt.
Resultado: activity_detail.xml e ImageDetailActivity.kt criados. AndroidManifest.xml e MainActivity.kt atualizados para suportar navegação entre ecrãs.

## Prompt 11
Objetivo: Implementar lógica de favoritos FIFO
Prompt utilizado: Executa o Passo 11: implementa a lógica de favoritos com uma fila FIFO de máximo 5 itens.
Resultado: FavoritesManager.kt criado com SharedPreferences e Gson. Lógica FIFO com máximo 5 itens implementada. ImageDetailActivity.kt atualizado com botão de favorito funcional.

## Prompt 12
Objetivo: Implementar cache de imagens
Prompt utilizado: Executa o Passo 12: implementa a cache de imagens com máximo 50 itens.
Resultado: CacheManager.kt criado com SharedPreferences e Gson. CatRepository.kt atualizado para guardar imagens na cache. MainViewModel.kt refatorado para AndroidViewModel para suportar Context.

## Prompt 13
Objetivo: Implementar acesso offline
Prompt utilizado: Executa o Passo 13: implementa o acesso offline usando os dados em cache.
Resultado: CatRepository.kt atualizado com estratégia fallback: tenta API, em caso de erro serve dados da cache local.

## Prompt 14
Objetivo: Tratamento de erros da API
Prompt utilizado: Executa o Passo 14: trata os erros da API de forma adequada no Repositório e no ViewModel.
Resultado: CatRepository.kt refatorado para devolver Result<List<CatImage>>. MainViewModel.kt atualizado para usar onSuccess e onFailure em vez de try-catch.

## Prompt 15
Objetivo: Configurar API key de forma segura
Prompt utilizado: Modifica o CatApiService.kt para incluir a API key no header x-api-key, lendo-a a partir do BuildConfig. Garante também que o build.gradle.kts lê a chave CAT_API_KEY do local.properties e a expõe via BuildConfig.
Resultado: API key configurada via local.properties e BuildConfig. build.gradle.kts atualizado com buildFeatures { buildConfig = true }. CatApiService.kt atualizado com @Header("x-api-key").

## Prompt 16
Objetivo: Compilar o projeto e gerar o APK
Prompt utilizado: Executa o Passo 15: compila o projeto, verifica que não há erros e gera o APK.
Resultado: Projeto compilado com sucesso após ajustes de configuração do Java SDK. APK gerado em app/build/outputs/apk/debug/app-debug.apk.

## Prompt 17
Objetivo: Identificar e corrigir problemas encontrados nos testes
Prompt utilizado: Durante os testes encontrei dois problemas: 1) No ecrã de detalhes algumas imagens aparecem em branco. 2) Não há forma de voltar ao ecrã principal a partir do ecrã de detalhes. Porque é que estes problemas estão a acontecer?
Resultado: Corrigido o carregamento de imagens HTTP com usesCleartextTraffic e fallback de erro no Glide. Adicionada Toolbar com botão de voltar no ecrã de detalhes.

## Prompt 18
Objetivo: Implementar Bottom Navigation Bar
Prompt utilizado: Lê o ficheiro docs/09_feature_extensions.md e implementa a Extensão 1: adiciona uma Bottom Navigation Bar com dois separadores — Galeria e Favoritos.
Resultado: GalleryFragment.kt e FavoritesFragment.kt criados. MainActivity refatorada para gerir fragmentos. bottom_nav_menu.xml criado com separadores Galeria e Favoritos.

## Prompt 19
Objetivo: Implementar ecrã de favoritos
Prompt utilizado: Implementa a Extensão 2: cria o ecrã de favoritos com RecyclerView mostrando as imagens favoritas e botão para remover cada uma.
Resultado: FavoritesViewModel.kt, FavoritesAdapter.kt e item_favorite.xml criados. FavoritesFragment.kt e FavoritesManager.kt atualizados com lógica de remoção e persistência.

## Prompt 20
Objetivo: Atualizar botão de favoritos no ecrã de detalhes
Prompt utilizado: Implementa a Extensão 3: atualiza o botão no ecrã de detalhes para mostrar se a imagem já é favorita ou não, e permite removê-la dos favoritos a partir desse ecrã.
Resultado: FavoritesManager atualizado com isFavorite(). Botão no ecrã de detalhes atualizado para alternar entre adicionar e remover com cores diferentes.

## Prompt 21
Objetivo: Melhorias visuais da app
Prompt utilizado: Implementa a Extensão 4: melhora o aspeto visual da app — tema personalizado, cores consistentes, cards com cantos arredondados e informação da imagem visível no ecrã de detalhes.
Resultado: colors.xml e themes.xml atualizados com paleta Indigo/Amber. item_cat.xml e item_favorite.xml com cantos arredondados 16dp. activity_detail.xml redesenhado com secção de informações da imagem (ID e dimensões).

## Prompt 22
Objetivo: Compilar o projeto e gerar o APK final
Prompt utilizado: Compila o projeto e gera o APK final atualizado com todas as alterações implementadas.
Resultado: Projeto compilado com sucesso utilizando JDK 25 e após execução de `clean` para resolver problemas de ambiente com o `jlink`. APK gerado em: `app/build/outputs/apk/debug/app-debug.apk`.

## Prompt 23
Objetivo: Corrigir dimensões 0x0 no ecrã de detalhes
Prompt utilizado: No ecrã de detalhes as dimensões aparecem como 0x0 pixels. Isto acontece porque a API não devolve width e height no endpoint atual. Corrige isto usando o endpoint https://api.thecatapi.com/v1/images/{id} para obter os detalhes completos da imagem incluindo as dimensões reais, quando o ecrã de detalhes é aberto.
Resultado: CatApiService.kt atualizado com endpoint de detalhe. CatRepository.kt atualizado com getCatImageDetail. DetailViewModel.kt criado. ImageDetailActivity.kt atualizado para mostrar dimensões reais após carregamento da API.

## Prompt 24
Objetivo: Mostrar informação de raça no ecrã de detalhes
Prompt utilizado: Atualiza a app para mostrar mais informação no ecrã de detalhes. Altera o endpoint de pesquisa para incluir has_breeds=true. Atualiza o modelo CatImage para incluir um array de breeds com os campos: name, origin, temperament, description e life_span. No ecrã de detalhes, mostra esses campos se estiverem disponíveis, e o URL da imagem. Se a imagem não tiver raça associada, mostra uma mensagem "Raça desconhecida".
Resultado: CatImage.kt atualizado com data class Breed. CatApiService.kt atualizado com has_breeds=1. activity_detail.xml expandido com secção de raça. ImageDetailActivity.kt atualizado com lógica de fallback para gatos sem raça.

## Prompt 25
Objetivo: Compilar o projeto final com informação de raças
Prompt utilizado: Compila o projeto e gera o APK final atualizado com todas as alterações implementadas.
Resultado: Projeto compilado com sucesso. APK final com suporte a metadados de raça, dimensões corrigidas e layout premium gerado em: `app/build/outputs/apk/debug/app-debug.apk`.

## Prompt 25
Objetivo: Melhorias finais na app
Prompt utilizado: Faz as seguintes melhorias na app: 1. Atualiza o README.md com uma descrição final do projeto, funcionalidades implementadas e instruções de execução. 2. Muda o ícone de remover nos favoritos para vermelho para ser mais intuitivo. 3. Quando o utilizador carrega no botão de atualizar na galeria, faz scroll automático para o topo da RecyclerView depois de carregar as novas imagens.
Resultado: README.md atualizado. Ícone de remover nos favoritos alterado para vermelho. Scroll automático para o topo implementado ao atualizar a galeria.

## Prompt 26
Objetivo: Compilar o APK final polido
Prompt utilizado: Compila o projeto e gera o APK final atualizado.
Resultado: APK final polido (ícone vermelho e scroll auto) gerado com sucesso em: `app/build/outputs/apk/debug/app-debug.apk`. Erro de compilação no Fragment e conflito de ambiente com o `jlink` resolvidos durante o processo.

## Prompt 27
Objetivo: Atualizar agents.md para permitir Jetpack Compose no módulo :app-compose
Prompt utilizado: Atualiza o ficheiro agents.md adicionando a seguinte regra: "O módulo :app-compose usa exclusivamente Jetpack Compose. A regra de XML Views aplica-se apenas ao módulo :app."
Resultado: agents.md atualizado com nova regra para o módulo :app-compose.

## Prompt 28
Objetivo: Adicionar dependências ao app-compose/build.gradle.kts
Prompt utilizado: Lê os ficheiros agents.md, docs/06_architecture.md, docs/08_implementation_plan.md e docs/09_feature_extensions.md e começa a implementar o módulo :app-compose passo a passo. Começa pelo Passo 1: adiciona as dependências necessárias ao app-compose/build.gradle.kts para Jetpack Compose, Coil, Navigation e pull-to-refresh.
Resultado: Adicionadas dependências de Navigation Compose, Coil e Lifecycle Runtime Compose ao libs.versions.toml e app-compose/build.gradle.kts.

## Prompt 29
Objetivo: Criar GalleryViewModel e FavoritesViewModel com StateFlow
Prompt utilizado: Sim, avança para o Passo 2.
Resultado: GalleryViewModel.kt e FavoritesViewModel.kt criados com StateFlow para imagens, loading e erros.

## Prompt 30
Objetivo: Criar GalleryScreen com LazyVerticalGrid e AnimatedVisibility
Prompt utilizado: Sim, avança para o Passo 3.
Resultado: GalleryScreen.kt criado com LazyVerticalGrid adaptativo (2 colunas portrait, 3 landscape) e AnimatedVisibility com fadeIn/fadeOut nos cards.

## Prompt 31
Objetivo: Criar DetailScreen com pinch-to-zoom e animateContentSize
Prompt utilizado: Sim, avança para o Passo 4.
Resultado: DetailScreen.kt e DetailViewModel.kt criados com pinch-to-zoom na imagem e animateContentSize no card de raça.

## Prompt 32
Objetivo: Criar FavoritesScreen
Prompt utilizado: Sim, avança para o Passo 5.
Resultado: FavoritesScreen.kt criado com LazyVerticalGrid adaptativo, gestão de favoritos e mensagem quando lista vazia.

## Prompt 33
Objetivo: Implementar pull-to-refresh na GalleryScreen
Prompt utilizado: Sim, avança para o Passo 6.
Resultado: PullToRefreshBox do Material 3 integrado na GalleryScreen ligado ao estado isLoading e ao fetchImages().

## Prompt 34
Objetivo: Implementar Light/Dark mode toggle
Prompt utilizado: Sim, avança para o Passo 7.
Resultado: MainScreen.kt criado com TopAppBar e botão sol/lua para alternar tema em tempo real. MainActivity.kt atualizado.

## Prompt 35
Objetivo: Implementar navegação com NavHost
Prompt utilizado: Sim, avança para o Passo 8.
Resultado: NavHost configurado no MainScreen.kt com rotas para gallery, favorites e detail. NavigationBar implementada com Bottom Navigation entre Galeria e Favoritos.

## Prompt 36
Objetivo: Gerar documentos finais (deliverables) do MIP-3
Prompt utilizado: Lê todos os ficheiros em docs/ e o agents.md e gera os três documentos finais pedidos como deliverables: docs/10_module_diagram.md com o diagrama de dependências entre módulos, docs/11_ui_contract.md descrevendo como cada módulo de UI interage com o :core, e docs/12_refactoring_plan.md explicando o que foi extraído do projeto original (antes da refatoração multi-módulo) para o módulo :core.
Resultado: Três documentos criados: 10_module_diagram.md com diagrama Mermaid, 11_ui_contract.md com contrato MVVM e StateFlow, 12_refactoring_plan.md com plano de refatoração detalhado.

## Prompt 37
Objetivo: Corrigir erros de compilação no módulo :app-compose
Prompt utilizado: Corrige os seguintes erros de compilação no módulo :app-compose:
1. MainActivity.kt linha 25: @Composable invocations can only happen from the context of a @Composable function
2. MainScreen.kt linhas 6 e 7: Unresolved reference 'Brightness4' e Unresolved reference 'Brightness7' — estes ícones não existem, substitui por ícones equivalentes disponíveis no androidx.compose.material.icons.Icons.
Resultado: MainActivity.kt corrigido movendo isSystemInDarkTheme() para fora do bloco remember. MainScreen.kt corrigido substituindo Brightness4 e Brightness7 por Icons.Default.Settings e Icons.Default.CheckCircle.
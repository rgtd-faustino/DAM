# Extensões de Funcionalidades

## Extensão 1: Navegação por Bottom Navigation Bar
Descrição: Barra de navegação inferior entre Galeria e Favoritos.
Módulos: :app (XML), :app-compose (NavHost)

## Extensão 2: Ecrã de Favoritos
Descrição: Ecrã dedicado com imagens favoritas (máximo 5, FIFO).
Módulos: :app (XML), :app-compose (Compose)

## Extensão 3: Remover dos Favoritos
Descrição: Remover favoritos a partir do ecrã de favoritos ou detalhes.
Módulos: :app (XML), :app-compose (Compose)

## Extensão 4: Melhorias Visuais
Descrição: Tema personalizado, cards arredondados, layout melhorado.
Módulos: :app (XML), :app-compose (MaterialTheme)

---

## Features Exclusivas do :app-compose

### Feature 1: Animações (AnimatedVisibility + animateContentSize)
Descrição: Cards de imagens aparecem e desaparecem com animação
fadeIn/fadeOut durante o carregamento. O card de detalhes de raça
expande/colapsa com animateContentSize ao carregar nele.
Localização: GalleryScreen.kt, DetailScreen.kt

### Feature 2: Dynamic Theming (Light/Dark mode)
Descrição: A app suporta tema claro e escuro usando MaterialTheme.
O utilizador pode alternar entre os dois modos dentro da app.
Localização: Theme.kt, MainScreen.kt

### Feature 3: Advanced Gestures (Pull-to-refresh + Pinch-to-zoom)
Descrição: O utilizador pode puxar a lista para baixo para atualizar
as imagens (pull-to-refresh). No ecrã de detalhes pode fazer
pinch-to-zoom na imagem do gato.
Localização: GalleryScreen.kt, DetailScreen.kt

### Feature 4: Adaptive Layouts (LazyVerticalGrid)
Descrição: Em modo portrait a galeria mostra 2 colunas. Em modo
landscape mostra 3 colunas. Usa LazyVerticalGrid com
GridCells.Adaptive para se adaptar ao tamanho do ecrã.
Localização: GalleryScreen.kt
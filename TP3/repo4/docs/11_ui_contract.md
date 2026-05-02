# Contrato de UI com o Módulo `:core`

Este documento descreve a forma como os módulos de interface gráfica (`:app` e `:app-compose`) interagem com a camada de dados residente no módulo `:core`.

## 1. Fonte Única de Verdade (Single Source of Truth)
O módulo `:core` expõe o **`CatRepository`**, que atua como a única fonte de verdade para a obtenção de dados. O repositório decide de forma transparente se os dados são obtidos pela rede (via `CatApiService`) ou a partir da cache (`CacheManager`).

## 2. Padrão de Interação (MVVM)
Ambos os módulos de UI seguem o padrão **Model-View-ViewModel (MVVM)**, comunicando com o `:core` da seguinte forma:

* **No módulo `:app` (XML):**
  * Os `ViewModels` instanciam ou recebem o `CatRepository` e outros Managers (ex. `FavoritesManager`).
  * Utilizam `LiveData` (ou equivalentes) para expor o estado e notificar a Activity/Fragment sobre atualizações.
  * As interações de UI (ex: click no botão refresh) invocam métodos no ViewModel, que por sua vez solicita os dados ao `:core`.

* **No módulo `:app-compose` (Jetpack Compose):**
  * Os `ViewModels` (ex: `GalleryViewModel`, `DetailViewModel`, `FavoritesViewModel`) interagem com o `:core` e convertem os dados usando fluxos reativos via `StateFlow` (`MutableStateFlow`).
  * A UI observa as atualizações e reconstrói o estado automaticamente utilizando `collectAsStateWithLifecycle()`.
  * Promove-se uma ligação mais declarativa, reativa e alinhada com as melhores práticas de concorrência modernas em Kotlin.

## 3. Gestão de Favoritos Persistente
A manutenção do estado e a persistência dos favoritos é responsabilidade exclusiva do **`FavoritesManager`** no `:core`.
* **Verificação**: Qualquer módulo pode verificar em tempo real se uma imagem é favorita através da chamada `isFavorite(id)`.
* **Modificação**: Os módulos acionam `addFavorite()` ou `removeFavorite()`. O `FavoritesManager` garante regras de negócio centralizadas, nomeadamente a capacidade máxima de 5 favoritos gerida através de uma lógica **FIFO** e persistência utilizando `SharedPreferences`.

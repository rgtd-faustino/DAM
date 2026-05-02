# Plano de Refatoração Multi-Módulo

Este documento detalha o que foi extraído do projeto original (antes da separação arquitetural) para criar o módulo independente `:core`. O objetivo da refatoração foi isolar a lógica de negócio e os dados da interface visual, permitindo a escalabilidade, independência e a reutilização do código por múltiplos módulos de interface (como o `:app` e o `:app-compose`).

## Componentes Extraídos para o `:core`

### 1. Modelos de Dados (`core.model`)
* Extraídos todos os data classes para representação dos domínios, como `CatImage` e `Breed`.
* Estes modelos foram desvinculados de qualquer import ou dependência relacionada a componentes visuais específicos do Android, focando-se estritamente na tipagem dos dados providenciados pela rede e base de dados.

### 2. Cliente de API e Rede (`core.api`)
* A interface Retrofit responsável pelos endpoints The Cat API (`CatApiService`) foi isolada no `:core`.
* O `:core` assume a responsabilidade pelas anotações de rede, serialização (`GsonConverterFactory`) e parâmetros HTTP.

### 3. Gestão Local / Cache (`core.data`)
* **`CacheManager`**: Foi extraído para encapsular as lógicas de leitura e gravação local da resposta da API utilizando `SharedPreferences` e a conversão de/para JSON com `Gson`. 
* **`FavoritesManager`**: Centralizou a lógica de manutenção das imagens favoritas, isolando regras de negócio sensíveis — como o limite máximo de 5 imagens favoritas e a respetiva rotatividade utilizando o conceito de fila *First In, First Out* (FIFO).

### 4. Repositório Central (`core.repository`)
* A classe **`CatRepository`** foi extraída para atuar como o elo de mediação principal, implementando o padrão de *Repository* recomendado no *Guide to App Architecture*.
* O repositório orquestra o `CatApiService` (para rede) e o `CacheManager` (para local), expondo um contrato claro e assíncrono (com *coroutines*) à camada superior, através da abstração de respotas ou erros sob o objeto nativo `Result<T>`.

## Benefícios e Impacto da Refatoração
1. **Desacoplamento Rigoroso**: Os processos computacionais complexos e a ligação com as APIs externas deixaram de estar disseminados ou expostos à UI.
2. **Elevada Reutilização**: A modularização garantiu que, perante o desafio de adotar a tecnologia Jetpack Compose num novo módulo `:app-compose`, nenhum código de rede ou gestão local tivesse de ser reescrito ou duplicado. Todo o `:core` funcionou de forma *"plug-and-play"*.
3. **Fronteiras Arquiteturais**: Assegurou uma arquitetura mais limpa e propensa a testes isolados (Test Driven Development) focados unicamente na validade do repositório ou dos modelos de negócio, ignorando dependências instáveis de layout.

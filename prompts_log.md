# Registo de Prompts

## Prompt 1
Objetivo: Adicionar dependências ao build.gradle
Prompt utilizado: Lê os ficheiros agents.md e docs/08_implementation_plan.md e executa o Passo 1: adiciona as dependências Retrofit, Glide, ViewModel e LiveData ao build.gradle.
Resultado: Dependências adicionadas ao build.gradle.

## Prompt 2
Objetivo: Adicionar permissão de Internet
Prompt utilizado: Executa o Passo 2: adiciona a permissão de Internet ao AndroidManifest.xml.
Resultado: Permissão de Internet adicionada ao AndroidManifest.xml.

## Prompt 3
Objetivo: Criar modelo de dados
Prompt utilizado: Executa o Passo 3: cria a classe de modelo de dados CatImage.kt.
Resultado: Classe CatImage.kt criada com os campos id, url e isFavourite.

## Prompt 4
Objetivo: Criar serviço de API com Retrofit
Prompt utilizado: Executa o Passo 4: cria a interface do serviço de API CatApiService.kt usando Retrofit.
Resultado: Interface CatApiService.kt criada com Retrofit.

## Prompt 5
Objetivo: Criar o Repositório
Prompt utilizado: Executa o Passo 5: cria a classe CatRepository.kt.
Resultado: Classe CatRepository.kt criada.

## Prompt 6
Objetivo: Criar o ViewModel
Prompt utilizado: Executa o Passo 6: cria o ViewModel MainViewModel.kt com LiveData<List<CatImage>>.
Resultado: MainViewModel.kt criado com LiveData<List<CatImage>>, estado de loading e erros.

## Prompt 7
Objetivo: Desenhar o layout activity_main.xml
Prompt utilizado: Executa o Passo 7: desenha o layout activity_main.xml com RecyclerView, botão de atualização e ProgressBar.
Resultado: Layout activity_main.xml criado com RecyclerView, botão de atualização e ProgressBar.

## Prompt 8
Objetivo: Criar o adaptador da RecyclerView
Prompt utilizado: Executa o Passo 8: cria o adaptador CatImageAdapter.kt usando Glide para carregar as imagens.
Resultado: CatImageAdapter.kt criado com Glide para carregar imagens.

## Prompt 9
Objetivo: Ligar MainActivity ao ViewModel
Prompt utilizado: Executa o Passo 9: liga a MainActivity.kt ao ViewModel e observa a LiveData para atualizar a interface.
Resultado: MainActivity.kt ligada ao ViewModel, observando LiveData para atualizar a interface.

## Prompt 10
Objetivo: Criar ecrã de detalhes
Prompt utilizado: Executa o Passo 10: cria o layout activity_detail.xml e a classe ImageDetailActivity.kt.
Resultado: Layout activity_detail.xml e ImageDetailActivity.kt criados.

## Prompt 11
Objetivo: Implementar lógica de favoritos FIFO
Prompt utilizado: Executa o Passo 11: implementa a lógica de favoritos com uma fila FIFO de máximo 5 itens.
Resultado: Lógica de favoritos implementada com fila FIFO de máximo 5 itens.

## Prompt 12
Objetivo: Implementar cache de imagens
Prompt utilizado: Executa o Passo 12: implementa a cache de imagens com máximo 50 itens.
Resultado: Cache de imagens implementada com máximo 50 itens.

## Prompt 13
Objetivo: Implementar acesso offline
Prompt utilizado: Executa o Passo 13: implementa o acesso offline usando os dados em cache.
Resultado: Acesso offline implementado usando dados em cache.

## Prompt 14
Objetivo: Tratamento de erros da API
Prompt utilizado: Executa o Passo 14: trata os erros da API de forma adequada no Repositório e no ViewModel.
Resultado: Tratamento de erros implementado no Repositório e no ViewModel.
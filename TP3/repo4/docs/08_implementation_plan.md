# Plano de Implementação

## Passo 1
Adicionar dependências ao build.gradle: Retrofit, Glide, ViewModel, LiveData.

## Passo 2
Adicionar permissão de Internet ao AndroidManifest.xml.

## Passo 3
Criar a classe de modelo de dados CatImage.kt.

## Passo 4
Criar a interface do serviço de API CatApiService.kt com Retrofit.

## Passo 5
Criar a classe Repositório CatRepository.kt.

## Passo 6
Criar o ViewModel MainViewModel.kt com LiveData<List<CatImage>>.

## Passo 7
Desenhar o layout activity_main.xml com RecyclerView, botão de atualização e ProgressBar.

## Passo 8
Criar o adaptador CatImageAdapter.kt usando Glide para carregar as imagens.

## Passo 9
Ligar a MainActivity.kt ao ViewModel e observar a LiveData para atualizar a interface.

## Passo 10
Criar activity_detail.xml e ImageDetailActivity.kt.

## Passo 11
Implementar a lógica de favoritos (fila FIFO, máximo 5 itens).

## Passo 12
Implementar cache de imagens (máximo 50 itens).

## Passo 13
Implementar acesso offline usando os dados em cache.

## Passo 14
Tratar erros da API de forma adequada no Repositório e no ViewModel.

## Passo 15
Compilar, instalar e testar no emulador.
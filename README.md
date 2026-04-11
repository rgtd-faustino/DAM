# MyCatGalleryApp 🐱

Aplicação Android moderna que permite navegar por uma vasta galeria de gatos fascinantes, gerir favoritos e explorar detalhes detalhados sobre raças felinas.

## ✨ Funcionalidades Implementadas

### 1. Galeria de Gatos
- Navegação fluida por imagens aleatórias de gatos.
- Botão de atualização para carregar novas imagens (com scroll automático para o topo).
- Carregamento assíncrono e eficiente com **Glide** e **Retrofit**.

### 2. Sistema de Favoritos (Premium)
- **Modo FIFO**: Guarda até 5 imagens favoritas. Ao adicionar a 6ª, a mais antiga é removida automaticamente.
- **Persistência Offline**: Os favoritos são guardados localmente usando `SharedPreferences` e `Gson`.
- **Navegação Dedicada**: Separador de favoritos exclusivo acessível via `BottomNavigationView`.

### 3. Ecrã de Detalhes Completo
- Visualização da imagem em alta resolução.
- **Metadados em Tempo Real**: Consulta a API para obter dimensões (px) e ID único.
- **Enciclopédia de Raças**: Exibe informações detalhadas (se disponíveis):
    - Nome da Raça e Origem.
    - Temperamento e Descrição.
    - Esperança de Vida.
- Botão "Toggle" para adicionar/remover favoritos diretamente do detalhe com feedback visual (cores).

### 4. Arquitetura e Tecnologia
- **MVVM (Model-View-ViewModel)** para separação de responsabilidades.
- **Material Design 3** com uma paleta de cores Indigo/Amber personalizada.
- **Coroutines** para operações assíncronas seguras.
- **Cache Local** para suporte básico de visualização em modo offline.

## 🛠️ Stack Tecnológica
- **Kotlin**: Linguagem principal.
- **Retrofit & Gson**: Comunicação com TheCatAPI.
- **Glide**: Renderização de imagens.
- **Material Components & CardViews**: UI moderna e arredondada.

## 🚀 Como Executar

1. **Clonar/Abrir o Projeto**: Importa o projeto no Android Studio.
2. **Configuração**: O projeto já inclui uma chave de API funcional (`CAT_API_KEY`) em `local.properties`.
3. **Build**: Executa `./gradlew assembleDebug` para compilar.
4. **Instalação**: Instala o APK gerado em `app/build/outputs/apk/debug/app-debug.apk` num emulador ou dispositivo físico com Android 7.0 (API 24) ou superior.

---
*Desenvolvido como projeto prático para demonstração de arquitetura Android moderna e integração de APIs REST.*
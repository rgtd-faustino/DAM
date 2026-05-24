# Assignment 4.1 & 4.2 —  Firebase

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 24/05/2026  
**Repository URL:** [DAM_TP4_Firebase](https://github.com/rgtd-faustino/DAM/edit/main/TP4/Firebase)


---

## 1. Introdução

Este relatório descreve o desenvolvimento das tarefas do Tutorial 4 da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM) do ISEL. O tutorial abrange três grandes temas: Kotlin Flows e coroutines para programação reativa, acesso a LLMs de IA (OpenAI GPTs e Google Gemini) via Kotlin, e a plataforma Google Firebase.

Este relatório cobre exclusivamente as secções **4.1 — Firebase very short introduction** e **4.2 — Codelab Friendly Chat**, ambas marcadas com `[AC YES, AI NO]`. As secções 2 (Kotlin Flows), 3 (AI LLMs) e 4.3 (Notes Pro) serão abordadas separadamente.

O código da aplicação Friendly Chat foi fornecido pelo professor no ficheiro `build-android-start.zip`, já completo à exceção do ficheiro `google-services.json`. As tarefas do aluno consistiram em ler as partes indicadas do codelab oficial, configurar o projeto Firebase na consola, obter e colocar o ficheiro de configuração correto, e realizar pequenas alterações ao código para corrigir comportamentos e adaptar ao ambiente real.

---

## 2. Visão Geral — Firebase (Secção 4.1)

O enunciado apresenta o Firebase como uma plataforma **Backend-as-a-Service (BaaS)** da Google, que fornece serviços de backend simplificados para aplicações móveis e web, sem necessidade de gerir infraestrutura de servidor própria.

Os serviços Firebase abordados no tutorial e relevantes para o projeto Friendly Chat são:

| Serviço | Descrição |
|---------|-----------|
| **Authentication** | Simplifica a autenticação de utilizadores para developers e end users |
| **Realtime Database** | Base de dados cloud onde os dados são armazenados em JSON e sincronizados em tempo real para todos os clientes ligados |
| **Cloud Storage** | Armazenamento de conteúdo gerado pelos utilizadores, como fotos ou vídeos |
| **Cloud Firestore** | Base de dados flexível e escalável para desenvolvimento mobile, web e server |
| **Cloud Functions** | Framework serverless para executar código backend em resposta a eventos |
| **Cloud Messaging** | Solução de messaging cross-platform |
| **Analytics** | Solução de medição de apps com insights sobre uso e engagement |
| **Crashlytics** | Solução de crash reporting |
| **Hosting** | Orquestra produtos Google Cloud para deploy e monitorização de web apps |

Para este tutorial, apenas os três primeiros serviços foram efetivamente utilizados: **Authentication**, **Realtime Database** e **Cloud Storage**.

---

## 3. Friendly Chat — Codelab (Secção 4.2)

### 3.1 Enquadramento

Conforme indicado no enunciado, o codelab seguido foi o **Firebase Android Codelab - Build Friendly Chat**. O projeto foi fornecido já completo no ficheiro `build-android-start.zip`, sendo um projeto XML Views. As partes do codelab lidas e executadas foram, conforme indicado no enunciado:

- **1. Overview** — visão geral da aplicação
- **6. Enable Authentication** — implementação da autenticação
- **7. Read Messages** — sincronização de mensagens com o Realtime Database
- **8. Send Messages** — envio de mensagens de texto e imagens
- **9. Congratulations!** — resumo do que foi implementado
- **10. Create and set up a Firebase project** — configuração do projeto Firebase real

A CLI do Firebase não foi utilizada, conforme indicado no enunciado.

### 3.2 Serviços Utilizados

O projeto Friendly Chat é uma aplicação de chat em tempo real onde múltiplos utilizadores autenticados podem trocar mensagens de texto e imagens, utilizando os seguintes serviços Firebase:

- **Firebase Authentication** — suporte à autenticação de utilizadores
- **Firebase Realtime Database** — armazenamento das mensagens dos utilizadores
- **Cloud Storage** — armazenamento de ficheiros binários (as imagens enviadas no chat)

### 3.3 Ficheiros do Projeto

Os ficheiros principais do projeto fornecido pelo professor são:

| Ficheiro | Responsabilidade |
|----------|-----------------|
| `MainActivity.kt` | Ecrã principal do chat — autenticação, lista de mensagens, envio de texto e imagens |
| `SignInActivity.kt` | Ecrã de login com FirebaseUI |
| `FriendlyMessageAdapter.kt` | Adaptador do RecyclerView para mensagens de texto e imagem |
| `FriendlyMessage.kt` | Data class que representa uma mensagem |
| `MyScrollToBottomObserver.kt` | Observer que faz scroll automático para a última mensagem |
| `MyButtonObserver.kt` | TextWatcher que ativa/desativa o botão de envio conforme o texto |
| `MyOpenDocumentContract.kt` | Contrato para abrir o seletor de imagens do sistema |

---

## 4. Arquitetura e Design

### 4.1 Firebase Authentication

A autenticação foi implementada com recurso à biblioteca **FirebaseUI**, que abstrai toda a complexidade de gerir sessões, tokens e interfaces de login. A lógica distribuiu-se por duas Activities:

- **SignInActivity** — Lança o ecrã de login da FirebaseUI e processa o resultado via `ActivityResultLauncher`. O enunciado refere que o projeto apresenta Email/Password e Sign in with Google, mas este último não foi configurado.
- **MainActivity** — Verifica nos métodos `onCreate()` e `onStart()` se existe um utilizador com sessão ativa. Caso não exista, redireciona imediatamente para a `SignInActivity`.

A verificação é feita em ambos os métodos intencionalmente: o `onCreate()` protege o carregamento inicial, e o `onStart()` protege o retorno ao app após este ter sido colocado em segundo plano.

### 4.2 Firebase Realtime Database

As mensagens são armazenadas no nó `messages` da Realtime Database. A sincronização em tempo real é feita através do `FirebaseRecyclerAdapter` da biblioteca FirebaseUI, que ouve automaticamente as alterações no nó e atualiza o `RecyclerView` sem necessidade de polling manual.

A opção `setLifecycleOwner(this)` no `FirebaseRecyclerOptions` foi usada para gerir automaticamente o início e paragem da escuta de dados em função do ciclo de vida da Activity, dispensando o controlo manual nos métodos `onPause()` / `onResume()` — que no código fornecido estão comentados precisamente por esta razão.

As regras de segurança configuradas garantem que apenas utilizadores autenticados podem ler e escrever mensagens:

```json
{
  "rules": {
    "messages": {
      ".read": "auth.uid != null",
      ".write": "auth.uid != null"
    }
  }
}
```

### 4.3 Cloud Storage

O envio de imagens segue um padrão de **UX otimista** já implementado no código fornecido: quando o utilizador seleciona uma imagem, é imediatamente criada uma mensagem temporária com um GIF de loading como placeholder. O upload para o Cloud Storage ocorre em paralelo, e quando termina com sucesso, o URL temporário é substituído pelo URL público permanente da imagem.

Esta abordagem, implementada nos métodos `onImageSelected()` e `putImageInStorage()` da `MainActivity`, garante feedback imediato ao utilizador mesmo que o upload demore alguns segundos.

As regras de segurança do Storage foram substituídas pelas geradas automaticamente (que usavam uma data de expiração) pela seguinte configuração baseada em autenticação:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 4.4 FriendlyMessageAdapter

O adaptador distingue dois tipos de mensagem através do método `getItemViewType()`:

- `VIEW_TYPE_TEXT` — para mensagens com campo `text` preenchido, usa o `MessageViewHolder`.
- `VIEW_TYPE_IMAGE` — para mensagens com `imageUrl`, usa o `ImageMessageViewHolder`.

O carregamento de imagens é feito com **Glide**, com suporte a URLs `gs://` (Cloud Storage) e URLs HTTP normais. Para URLs do tipo `gs://`, é primeiro obtido o `downloadUrl` público antes de carregar com Glide.

---

## 5. Configuração do Projeto Firebase

Conforme indicado no enunciado na secção 10 do codelab, foram executados os seguintes passos:

### 5.1 Criar um projeto Firebase

Projeto criado na consola Firebase com o nome **Firebase - DAM**. O Google Analytics foi desativado por não ser necessário.

### 5.2 Upgrade do plano de preços

O Cloud Storage requer o plano **Blaze** (pagamento por utilização). O upgrade foi realizado associando uma conta de faturamento do Google Cloud. O bucket foi criado na região **US-EAST1**, que beneficia do nível "Sempre sem custos financeiros" do Google Cloud Storage.

### 5.3 Adicionar Firebase ao projeto Android

A aplicação foi registada na consola Firebase com o package name `com.google.firebase.codelab.friendlychat`. Os dois primeiros parágrafos foram saltados conforme indicado no enunciado. O ficheiro `google-services.json` foi descarregado e colocado na pasta `app/`, seguido de sincronização via **File → Sync Project with Gradle Files**.

### 5.4 Configurar Firebase Authentication

Conforme indicado no enunciado — **apenas Email/Password** foi ativado.

### 5.5 Configurar Realtime Database

Base de dados criada na região **europe-west1** (Bélgica), em modo de teste, com regras de segurança baseadas em autenticação.

### 5.6 Configurar Cloud Storage

Storage criado na região **US-EAST1**, em modo de teste, com regras de segurança baseadas em autenticação.

> **Nota:** O último tópico da secção 10 do codelab — *Connect to Firebase resources* — não foi executado, conforme indicado no enunciado: *"the last topic: Connect to Firebase resources, is not necessary as that code is already removed"*.

---

## 6. Alterações Realizadas ao Código Fornecido

O código foi entregue já funcional. As únicas alterações realizadas foram:

- **`SignInActivity.kt`** — O tema do ecrã de login foi alterado de `AppTheme` para `AppThemeNoActionBar` para contornar o bug visual reportado no enunciado, onde a `ActionBar` sobrepõe o campo de email. Conforme sugerido no enunciado: *"we suggests that you starts the tests by having a NoActionBar theme"*.

- **`google-services.json`** — O ficheiro inicial descarregado não continha o campo `firebase_url` porque a Realtime Database ainda não tinha sido criada quando o ficheiro foi gerado pela primeira vez. Foi descarregada uma versão atualizada após a configuração de todos os serviços.

Não foram feitas alterações funcionais ao código Kotlin fornecido pelo professor.

---

## Autonomous Software Engineering Sections

As secções 7 a 11 não se aplicam a este trabalho. Todo o código foi desenvolvido com `[AC YES, AI NO]`, pelo que não houve recurso a ferramentas de geração de código por IA. A IA foi utilizada apenas na redação do relatório (ver secção 15).

---

## Development Process

### 12. Version Control and Commit History

Os commits foram feitos de forma incremental, primeiro a configuração do projeto Firebase na consola e depois os ajustes ao código. Esta metodologia permitiu isolar facilmente a origem de problemas, como o caso do `google-services.json` sem o campo `firebase_url`.

### 13. Dificuldades e Lições Aprendidas

- **`google-services.json` sem `firebase_url`:** O ficheiro descarregado inicialmente não continha o URL da Realtime Database porque a base de dados ainda não tinha sido criada quando o ficheiro foi gerado. A lição é sempre descarregar um novo `google-services.json` após configurar todos os serviços Firebase, e não apenas no início do registo da aplicação.

- **Regras de Storage no modo de teste:** As regras geradas automaticamente pelo Firebase usam uma data de expiração (`request.time < timestamp.date(...)`) em vez de verificar autenticação. Estas foram substituídas por `request.auth != null` para um controlo mais adequado e sem prazo de validade.

- **Plano Blaze necessário para Storage:** O Cloud Storage não está disponível no plano Spark. O upgrade para Blaze foi necessário, mas o bucket foi criado na região US-EAST1 para aproveitar o nível sem custos financeiros.

- **Bug visual no ecrã de login:** Conforme alertado no enunciado, a `ActionBar` sobrepõe o campo de email na `SignInActivity`. A solução passou por alterar o tema para `AppThemeNoActionBar` no `onStart()` da `SignInActivity`.

### 14. Future Improvements

- **Sign in with Google:** O projeto já tem o provider Google configurado no código da `SignInActivity`, mas não foi ativado por não estar no âmbito do tutorial. Configurar a impressão digital SHA permitiria ativar este método de login.
- **Regras de segurança mais granulares:** As regras atuais permitem que qualquer utilizador autenticado leia todas as mensagens. Em produção, seria mais adequado restringir o acesso por utilizador ou por sala de chat.
- **Paginação das mensagens:** Atualmente são carregadas todas as mensagens de uma vez. Para chats com muitas mensagens, seria importante implementar paginação com `limitToLast()` na query do Realtime Database.

### 15. AI Usage Disclosure

**Código:** `[AC YES, AI NO]`  
O código foi fornecido pelo professor no ficheiro `build-android-start.zip`. As únicas alterações realizadas foram a correção do tema na `SignInActivity` e a substituição do `google-services.json`, ambas feitas manualmente sem recurso a ferramentas de geração de código por IA.

**Relatório:** `[AC YES, AI YES]`  
A redação e estruturação deste relatório foram assistidas pelo modelo Claude (Anthropic). O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.

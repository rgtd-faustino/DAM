# Assignment 4.3 & 4.3.1 — Firebase: Notes Pro App

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 24/05/2026  
**Repository URL:** https://github.com/rgtd-faustino/DAM_TP4_Firebase

---

## High-Level Project Description

A **Notes Pro App** é uma aplicação Android desenvolvida no âmbito das secções 4.3 e 4.3.1 do Tutorial 4 da unidade curricular de Desenvolvimento de Aplicações Móveis. O projeto parte de uma base de código Java fornecida pelo professor e é concluído e estendido em Kotlin, integrando os serviços Firebase Authentication e Cloud Firestore para persistência e autenticação na cloud.

A secção 4.3 consiste em completar a aplicação a partir do ponto onde o código fornecido termina, nomeadamente implementar a listagem de notas numa RecyclerView com dados provenientes do Firestore em tempo real — esta secção foi realizada inteiramente pelo aluno sem assistência de IA. A secção 4.3.1 adiciona duas funcionalidades com assistência de IA: suporte a **imagens opcionais** em cada nota (com armazenamento no Firebase Storage) e uma funcionalidade GOAT — o sistema de **bloqueio temporal de notas por data**, onde uma nota pode ser bloqueada até uma data futura definida pelo utilizador.

---

## Application Purpose

O objetivo principal desta aplicação é demonstrar a integração prática de vários serviços Firebase num projeto Android moderno escrito em Kotlin. A aplicação permite aos utilizadores:

- Criar e editar notas com título, conteúdo e imagem opcional.
- Autenticar-se com email e password, com verificação obrigatória por email.
- Guardar e consultar as suas notas em tempo real através do Cloud Firestore.
- Associar uma imagem a cada nota, guardada no Firebase Storage e carregada de forma eficiente com Glide.
- Bloquear notas até uma data futura, impedindo o acesso ao conteúdo até essa data chegar.

---

## 1. Introdução

Este relatório descreve o desenvolvimento das secções 4.3 e 4.3.1 do Tutorial 4. O projeto base fornecido estava escrito em Java e incompleto — o código de autenticação, criação e edição de notas estava presente, mas a listagem de notas na interface principal não estava implementada.

A secção 4.3, classificada como [AC YES, AI NO], foi realizada inteiramente pelo aluno: conversão do código Java para Kotlin, completar a implementação da RecyclerView com integração Firestore em tempo real, e configuração do Firebase. A secção 4.3.1, classificada como [AC YES, AI YES], envolveu assistência de IA para adicionar imagens opcionais nas notas e para implementar a funcionalidade GOAT — o sistema de bloqueio temporal de notas.

A funcionalidade GOAT implementada foi o sistema de bloqueio temporal — o utilizador pode definir uma data futura e a nota fica inacessível até essa data, mostrando uma mensagem de contagem e escondendo o conteúdo.

---

## 2. Visão Geral do Sistema

A aplicação está organizada em torno de cinco ecrãs principais:

- **SplashActivity**: ecrã de arranque que verifica se existe sessão ativa e redireciona para o login ou para a lista de notas.
- **LoginActivity**: autenticação por email e password com verificação de email obrigatória antes de permitir o acesso.
- **CreateAccountActivity**: criação de conta com validação de email, password e confirmação, seguida de envio automático de email de verificação.
- **MainActivity**: lista de notas do utilizador autenticado, ordenadas por data descendente, carregadas em tempo real a partir do Firestore através de um `FirestoreRecyclerAdapter`.
- **NoteDetailsActivity**: criação e edição de notas, com suporte a imagem (galeria do telemóvel + upload para Firebase Storage) e definição de data de desbloqueio.

A autenticação é gerida pelo Firebase Authentication, os dados das notas (título, conteúdo, timestamp, URL da imagem e data de desbloqueio) ficam no Cloud Firestore, e os ficheiros de imagem ficam no Firebase Storage.

---

## 3. Arquitetura e Design

### Estrutura do Projeto

```
app/src/main/java/com/notes/notesproxmlviews/
├── SplashActivity.kt
├── LoginActivity.kt
├── CreateAccountActivity.kt
├── MainActivity.kt
├── NoteDetailsActivity.kt
├── NoteAdapter.kt
├── Note.java
└── Utility.java
```

### Decisões de Design

**Coexistência Kotlin/Java:** O projeto mantém os ficheiros `Note.java` e `Utility.java` em Java, uma vez que o Kotlin e o Java coexistem sem problemas no mesmo projeto Android. Não havia necessidade de converter esses ficheiros — o importante era completar e estender a aplicação em Kotlin como pedido.

**FirestoreRecyclerAdapter:** Em vez de gerir manualmente os listeners do Firestore, usa-se o `FirestoreRecyclerAdapter` da biblioteca `firebase-ui-firestore`. Este adapter trata automaticamente de observar a query do Firestore e atualizar a RecyclerView quando os dados mudam, eliminando código de gestão de estado manual e evitando memory leaks ao ligar e desligar o listener em `onStart`/`onStop`.

**Firebase Storage com getInstance() sem argumentos:** A referência ao Storage usa `FirebaseStorage.getInstance()` sem argumentos, deixando que o SDK leia o bucket correto diretamente do `google-services.json`. Esta abordagem é mais robusta e elimina erros por nome de bucket incorreto.

**Sistema de bloqueio temporal (GOAT):** A data de desbloqueio é guardada como `Timestamp` no Firestore no campo `unlockDate`. O `NoteAdapter` compara esse timestamp com a data atual a cada render — se a nota ainda está bloqueada, esconde o conteúdo, aplica transparência ao título e à imagem, e desativa o clique para abrir a nota. Quando a data passa, a nota desbloqueia automaticamente na próxima vez que a lista é renderizada.

---

## 4. Implementação

### Tecnologias e Bibliotecas

| Biblioteca | Função |
|---|---|
| Firebase Authentication | Autenticação por email/password com verificação de email |
| Cloud Firestore | Base de dados para notas (título, conteúdo, timestamp, imageUrl, unlockDate) |
| Firebase Storage | Armazenamento dos ficheiros de imagem das notas |
| firebase-ui-firestore 8.0.1 | `FirestoreRecyclerAdapter` para sincronização automática Firestore ↔ RecyclerView |
| Glide 4.16.0 | Carregamento eficiente de imagens a partir de URLs com cache automática |

### Detalhe de Implementação

**NoteAdapter.kt — listagem com estados de bloqueio:**  
O adapter é o componente central da funcionalidade de bloqueio. Para cada nota, compara `note.unlockDate` com `Date()` atual. Se a nota está bloqueada, a descrição fica com `visibility = GONE`, o título e a imagem ficam com `alpha = 0.4f`, e o clique mostra um toast com a data de desbloqueio em vez de abrir a nota. Se está desbloqueada mas tem data de desbloqueio definida, mostra "Abriu em [data]". O reset explícito de `visibility` e `alpha` em todas as branches do `if/else` é crítico para evitar comportamentos errados causados pela reutilização de views no RecyclerView.

**NoteDetailsActivity.kt — upload de imagens:**  
O utilizador escolhe uma imagem com `registerForActivityResult(GetContent())`. O URI local é guardado em `selectedImageUri`. Ao guardar a nota, se há uma imagem nova, o upload para o Storage é feito primeiro — só depois de obter o URL público com `storageRef.downloadUrl` é que a nota é guardada no Firestore com esse URL. Se não há imagem nova mas a nota já tinha uma (`existingImageUrl`), esse URL é mantido para não se perder a imagem ao editar apenas o texto.

**Sistema de bloqueio — DatePickerDialog:**  
O `lockNoteBtn` abre um `DatePickerDialog` configurado com `datePicker.minDate = System.currentTimeMillis() + 86400000L` para só permitir datas de amanhã para a frente. A data escolhida é convertida para `com.google.firebase.Timestamp` ao guardar a nota.

---

## 5. Testes e Validação

Foram realizados testes manuais no dispositivo físico do aluno cobrindo os seguintes cenários:

| Cenário | Resultado |
|---|---|
| Criar conta e verificar email | ✓ Passou |
| Login com email não verificado | ✓ Rejeitado com mensagem |
| Criar nota sem imagem | ✓ Passou |
| Criar nota com imagem | ✓ Imagem aparece no Firestore e na lista |
| Editar nota mantendo imagem existente | ✓ Imagem preservada |
| Bloquear nota com data futura | ✓ Nota aparece bloqueada com "Abre em [data]" |
| Nota desbloqueada mostra "Abriu em [data]" | ✓ Passou |
| Logout e re-login | ✓ Notas do utilizador carregam corretamente |
| Scroll na lista com notas mistas (com e sem imagem) | ✓ Sem comportamentos errados de reciclagem |

---

## 6. Instruções de Utilização

### Pré-requisitos

- Android Studio (versão recente com suporte a AGP 8.10.0)
- JDK JetBrains Runtime 21 (incluído no Android Studio)
- Projeto Firebase configurado com Authentication (Email/Password), Cloud Firestore e Storage ativos

### Configuração

1. Clonar o repositório.
2. No Firebase Console, criar um projeto, ativar Authentication (Email/Password), Firestore e Storage.
3. Descarregar o `google-services.json` e colocá-lo na pasta `app/` do projeto.
4. Sincronizar o projeto com Gradle (File → Sync Project with Gradle Files).

### Execução

Correr a aplicação no Android Studio com Run → Run 'app' num dispositivo ou emulador com Android API 24+.

---

## Autonomous Software Engineering Sections

---

## 7. Prompting Strategy

A secção 4.3 [AC YES, AI NO] foi realizada inteiramente pelo aluno sem qualquer assistência de IA. Para a secção 4.3.1 [AC YES, AI YES], o desenvolvimento utilizou o **agente Claude (Anthropic)** através da aplicação **Antigravity** como assistente. A estratégia de prompting foi iterativa e contextual — o aluno foi partilhando o código e os erros à medida que surgiam, e o agente respondia com blocos FIND/REPLACE prontos a aplicar diretamente no Android Studio.

| Componente | Descrição |
|---|---|
| Context | Projeto Android com código Java base, a ser estendido em Kotlin com Firebase Storage e funcionalidade GOAT |
| Goal | Adicionar imagens opcionais nas notas e implementar bloqueio temporal como funcionalidade GOAT |
| Constraints | Manter coexistência Kotlin/Java, usar firebase-ui-firestore, seguir o estilo de código já existente |
| Plan | Iteração incremental: primeiro as imagens, depois o sistema de bloqueio |
| Verification | Teste manual no dispositivo após cada alteração, com reporte de erros ao agente |
| Deliverables | Código Kotlin completo, com comentários escritos pelo aluno no mesmo tom do código existente |

Uma decisão de prompting relevante foi pedir explicitamente que todas as alterações fossem entregues em pares FIND/REPLACE, permitindo ao aluno aplicá-las com Ctrl+R no Android Studio sem ambiguidade. Os comentários do código foram escritos pelo próprio aluno, garantindo que refletem a sua compreensão real do que cada parte faz.

---

## 8. Autonomous Agent Workflow

O agente Claude através da aplicação Antigravity contribuiu exclusivamente na secção 4.3.1. A secção 4.3 foi realizada inteiramente pelo aluno.

**Implementação de imagens (4.3.1):**  
O agente orquestrou as alterações nos 7 ficheiros necessários (libs.versions.toml, build.gradle.kts, Note.java, activity_note_details.xml, NoteDetailsActivity.kt, recycler_note_item.xml, NoteAdapter.kt) para adicionar suporte a imagens opcionais. Diagnosticou e resolveu o bug do upload falhado ao identificar que usar `FirebaseStorage.getInstance()` sem argumentos era mais robusto do que hardcodar o URL do bucket.

**Funcionalidade GOAT — bloqueio temporal (4.3.1):**  
Implementou o sistema de bloqueio por data, incluindo o `DatePickerDialog`, o campo `unlockDate` na classe `Note`, a lógica de comparação de datas no adapter e os estados visuais diferenciados para notas bloqueadas vs. desbloqueadas.

**Debugging (4.3.1):**

| Problema | Solução |
|---|---|
| "Failed to upload image" com erro 404 | Substituição de `getInstance("gs://...")` por `getInstance()` sem argumentos |
| Botão de imagem sobreposto ao conteúdo | Reestruturação do layout com `ScrollView` e elementos dentro do cartão branco |
| Views bloqueadas ficavam com estado errado após scroll | Reset explícito de `visibility` e `alpha` em ambas as branches do `if/else` do adapter |

**Intervenção humana (4.3.1):**

- Definir o conceito da funcionalidade GOAT (bloqueio temporal de notas).
- Reportar cada erro com o contexto suficiente (mensagem de erro, código atual, comportamento observado).
- Escrever todos os comentários do código.
- Testar manualmente cada alteração no dispositivo.
- Identificar que o comportamento de esconder a descrição nas notas bloqueadas era o comportamento desejado e pedir que fosse tornado intencional.

---

## 9. Verification of AI-Generated Artifacts

O código gerado pelo agente foi verificado através de testes manuais no dispositivo físico do aluno. A verificação incluiu:

- **Compilação após cada alteração:** cada par FIND/REPLACE foi aplicado e o projeto compilado antes de avançar.
- **Teste funcional no dispositivo:** cada funcionalidade foi testada manualmente — adicionar imagem, bloquear nota, verificar estado visual, testar scroll da lista.
- **Revisão do código gerado:** o aluno leu cada bloco de código gerado e escreveu os comentários explicativos, o que obrigou a compreender o que cada parte faz.
- **Verificação do Firestore:** após criar notas com imagem e com data de bloqueio, o aluno verificou no Firebase Console que os campos `imageUrl` e `unlockDate` estavam a ser guardados corretamente.

---

## 10. Human vs AI Contribution

| Área | Responsável |
|---|---|
| **Secção 4.3 — completa** | Humano |
| Configuração Firebase (Authentication, Firestore, Storage) | Humano |
| Conversão de código Java para Kotlin | Humano |
| `NoteAdapter.kt` — implementação inicial da RecyclerView | Humano |
| `MainActivity.kt` — setupRecyclerView, onStart/onStop/onResume | Humano |
| **Secção 4.3.1 — imagens e GOAT** | |
| Conceito e definição da funcionalidade GOAT | Humano |
| `NoteDetailsActivity.kt` — lógica de upload de imagens e bloqueio | IA (agente Claude via Antigravity) |
| `NoteAdapter.kt` — estados visuais de bloqueio temporal | IA (agente Claude via Antigravity) |
| `Note.java` — adição dos campos imageUrl e unlockDate | IA (agente Claude via Antigravity) |
| Layouts XML (imagem, botão de bloqueio, DatePickerDialog) | IA (agente Claude via Antigravity) |
| Comentários em todo o código | Humano |
| Testes manuais e validação no dispositivo | Humano |
| Verificação do Firestore Console | Humano |

---

## 11. Ethical and Responsible Use

O uso do agente neste projeto foi transparente e limitado à secção que o permite [AC YES, AI YES]. A secção 4.3, classificada como [AC YES, AI NO], foi realizada sem qualquer assistência de IA, respeitando as regras do enunciado. Os principais cuidados na secção 4.3.1 foram:

| Risco | Mitigação |
|---|---|
| Código gerado sem compreensão | O aluno leu cada bloco gerado e escreveu os comentários, o que obrigou a compreender o funcionamento real |
| Alterações destrutivas por FIND/REPLACE errado | Cada par foi aplicado com atenção e o projeto compilado de seguida |
| Perda de dados do Firestore | Verificação manual no Firebase Console após cada alteração que tocasse na estrutura dos dados |

---

## Development Process

---

## 12. Version Control and Commit History

O repositório utiliza uma única branch main, adequada para um projeto académico individual. Os commits refletem a progressão incremental do projeto: setup inicial e configuração Firebase, implementação da RecyclerView e listagem de notas (4.3), e por fim adição de imagens e funcionalidade de bloqueio temporal (4.3.1).

---

## 13. Difficulties and Lessons Learned

### Problemas Encontrados

**Android Studio — Vista Android não aparecia:**  
Ao importar o projeto, o dropdown do painel lateral só mostrava a vista "Project" e não a vista "Android". O problema estava no JDK configurado para o Gradle — o ficheiro `.gradle/config.properties` tinha um caminho de JDK de outro computador. A solução foi mudar manualmente para o JetBrains Runtime 21 em File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK.

**Notas não apareciam na lista após serem criadas:**  
O código da RecyclerView estava implementado no método `setupRecyclerView()` mas esse método nunca era chamado no `onCreate()` do `MainActivity.kt`. As notas eram criadas corretamente no Firestore (confirmado na consola) mas a lista ficava sempre vazia. Bastou adicionar a chamada `setupRecyclerView()` no `onCreate()`.

**Hint text invisível nos campos de texto:**  
Os campos `EditText` do `activity_note_details.xml` não tinham `android:textColorHint` definido. Como o fundo do card era branco e a cor de hint por defeito do tema também era clara, os placeholders ficavam invisíveis — branco sobre branco. A solução foi adicionar `android:textColorHint="#888888"` a ambos os campos.

**Descrição das notas invisível na lista:**  
O `note_content_text_view` no `recycler_note_item.xml` não tinha `android:textColor` definido. O título tinha `android:textColor="@color/black"` mas a descrição não — como o fundo do card é branco, o texto ficava invisível. A solução foi adicionar `android:textColor="@color/black"` ao campo de conteúdo.

---

## 14. Future Improvements

- **Pesquisa de notas:** Adicionar uma barra de pesquisa na `MainActivity` para filtrar notas por título em tempo real.
- **Notificação de desbloqueio:** Usar `WorkManager` para enviar uma notificação local quando uma nota bloqueada chega à data de desbloqueio.
- **Eliminar imagem do Storage ao apagar nota:** Atualmente, ao apagar uma nota, o documento do Firestore é removido mas o ficheiro de imagem fica no Firebase Storage. Seria necessário também eliminar o ficheiro do Storage para não deixar dados órfãos.

---

## 15. AI Usage Disclosure (Mandatory)

**Código:** [AC YES, AI NO] para a secção 4.3 | [AC YES, AI YES] para a secção 4.3.1  
A secção 4.3 foi desenvolvida inteiramente pelo aluno sem assistência de IA. A secção 4.3.1 foi desenvolvida com assistência do agente Claude (Anthropic) através da aplicação Antigravity. O aluno orientou o desenvolvimento através de uma sessão iterativa, partilhando código e erros e aplicando as alterações sugeridas. Todo o código foi testado manualmente no dispositivo físico pelo aluno. Todos os comentários presentes no código foram escritos pelo aluno.

| Ferramenta | Utilização |
|---|---|
| Antigravity (agente Claude) | Geração de código Kotlin para 4.3.1, blocos FIND/REPLACE, diagnóstico de erros |

O aluno compreende o código produzido, é capaz de o explicar e assume total responsabilidade pelo produto final.

**Relatório:** [AC YES, AI YES]  
A redação e estruturação deste relatório foi assistida pelo modelo Claude (Anthropic). O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.
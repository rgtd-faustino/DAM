<<<<<<< HEAD
---
# Tutorial 1 — System Info App

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 10/03/2026  
**Repository URL:** https://github.com/GameDevRafael/DAM_TP1_SystemInfo

---

## 1. Introdução

Esta aplicação foi desenvolvida como parte da secção 5.3 do Tutorial 1 da disciplina e representa o primeiro projeto Android com propósito funcional, depois do exercício inicial de Hello World. O objetivo é ler diretamente dados do dispositivo através da classe `android.os.Build` pertencente ao SDK do Android, e apresentar esses dados de forma legível num único ecrã. Não existem interações com o utilizador — trata-se de uma aplicação de visualização de informação de sistema.

## 2. Visão Geral do Sistema

A aplicação funciona num único ecrã e não requer permissões especiais além das predefinidas. Ao iniciar, acede à classe `android.os.Build` nativa do Android para recolher propriedades do dispositivo como a marca, modelo, nível de API, versão do sistema operativo, entre outras. Essa informação é formatada numa única string e apresentada num `TextView` multilinha centrado no ecrã.

## 3. Arquitetura e Design

A estrutura de ficheiros segue o padrão gerado pelo Android Studio para um projeto de atividade única. O nome do package é `dam_A51394.helloworldoptional`, uma vez que o projeto foi desenvolvido a partir de uma base já existente, para evitar configurar do zero novamente todo o ambiente.
=======
# Assignment 5 — Building a System Info App
Course: Desenvolvimento de Aplicações Móveis
Student(s): A51394 Rafael Faustino
Date: 08/03/2026
Repository URL: https://github.com/GameDevRafael/DAM_TP1_SystemInfo

## 1. Introduction
O objetivo deste exercício era criar uma aplicação Android simples que lesse e mostrasse informações sobre o hardware e o sistema operativo do dispositivo. Serviu como uma introdução às APIs nativas do Android, permitindo-me perceber como uma app consegue aceder a dados do sistema onde está a correr. A informação recolhida devia ser apresentada num componente de texto multilinha (MultiLine TextView).

## 2. System Overview
A "System Info App" é uma aplicação de ecrã único. Ao abrir, vai buscar um conjunto de informações sobre o dispositivo (físico ou virtual) e apresenta tudo formatado na interface principal. O resultado é parecido com a imagem de referência do enunciado, mostrando os dados diretamente no ecrã.

## 3. Architecture and Design
A app tem uma única atividade, a `MainActivity`. O layout XML contém apenas um `TextView` configurado para mostrar múltiplas linhas de texto. Não há arquitetura complexa — assim que o `onCreate` é executado, os dados são lidos das classes do sistema Android e passados diretamente para a UI.

## 4. Implementation
Não precisei de adicionar nenhuma dependência externa. Usei apenas as bibliotecas padrão do Android, nomeadamente a classe `android.os.Build` para aceder às informações do sistema e `android.widget.TextView` para as mostrar.

Os campos que mapeei foram:
- **Manufacturer:** `Build.MANUFACTURER`
- **Brand:** `Build.BRAND`
- **Device:** `Build.DEVICE`
- **Model:** `Build.MODEL`
- **Android Version:** `Build.VERSION.RELEASE`
- **SDK Version:** `Build.VERSION.SDK_INT`
- Entre outros (display, incremental build, etc.).

Para formatar o texto usei raw strings do Kotlin (`"""..."""`) com `.trimIndent()`, o que tornou o código mais limpo do que concatenar strings manualmente:
>>>>>>> 95e44dbe5ed37081591c778c9846e31f65bf5c45

No layout definido em `activity_main.xml`, foi utilizado o `ConstraintLayout` como raiz, por ser o layout recomendado para posicionamento relativo de elementos. A interface é composta por três elementos principais:
- Um `TextView` de título fixo no topo, com fundo verde (`#37B61B`);
- Um `ImageView` com uma imagem de engrenagem ao centro, a representar o tema de configurações de sistema;
- Um `TextView` multilinha abaixo, que recebe e apresenta os dados obtidos em tempo de execução.

A fonte `monospace` foi definida no `TextView` de resultados para melhorar a legibilidade dos dados crus de hardware e software, tirando partido do alinhamento natural das colunas que este tipo de letra proporciona.

## 4. Implementação

O foco da implementação foi aceder à classe `android.os.Build` e extrair as propriedades relevantes. O `Build` disponibiliza constantes estáticas como `BRAND`, `MODEL`, `MANUFACTURER`, além de um objeto aninhado `Build.VERSION` que contém `RELEASE` (versão legível do Android) e `SDK_INT` (nível da API).

Para construir a string de saída, foi utilizada uma raw string do Kotlin (`""" """`), que permite escrever texto multilinha com interpolação direta de variáveis (`${}`). A função `trimIndent()` foi aplicada no final para remover a indentação do código-fonte, garantindo que o texto não fica desalinhado quando apresentado no ecrã.

Excerto principal:
```kotlin
val info = """
    Brand: ${Build.BRAND}
    Model: ${Build.MODEL}
    Manufacturer: ${Build.MANUFACTURER}
    Device: ${Build.DEVICE}
    Android Version: ${Build.VERSION.RELEASE}
    API Level: ${Build.VERSION.SDK_INT}
    Board: ${Build.BOARD}
    Hardware: ${Build.HARDWARE}
    Product: ${Build.PRODUCT}
    Display: ${Build.DISPLAY}
""".trimIndent()

systemInfo.setText(info)
```

<<<<<<< HEAD
A referência ao `TextView` (`systemInfo`) foi obtida através do `findViewById()` no método `onCreate()` da `MainActivity`.

## 5. Testes e Validação

Os testes foram realizados exclusivamente através do emulador disponibilizado pelo Android Studio. O objetivo foi verificar que todas as chamadas a `Build` retornavam valores válidos e que a formatação com `trimIndent()` produzia o resultado visual esperado. Todos os campos apresentaram valores corretos correspondentes ao hardware simulado, sem erros ou valores nulos.

## 6. Instruções de Utilização

Para clonar e executar o projeto:
1. Clonar o repositório: `git clone https://github.com/GameDevRafael/DAM_TP1_SystemInfo.git`
2. Abrir o Android Studio e selecionar "Open", apontando para a pasta clonada.
3. Aguardar que o Gradle sincronize e descarregue as dependências necessárias.
4. Ligar um dispositivo físico via USB ou configurar um emulador no AVD Manager.
5. Executar a aplicação com o botão "Run" (▶).
=======
## 5. Testing and Validation
Não tinha um dispositivo físico disponível, por isso testei tudo num emulador Android dentro do Android Studio. Verifiquei que os dados apareciam corretamente — o fabricante aparecia como "Google", o modelo mostrava os metadados do dispositivo virtual, e o texto multilinha renderizava bem, de acordo com a imagem de referência do enunciado.

## 6. Usage Instructions
1. Fazer clone do repositório para o computador.
2. Abrir a pasta raiz do projeto no Android Studio.
3. Aguardar o fim do Gradle Sync.
4. Compilar e executar com o botão Run, com um emulador já iniciado.
5. O ecrã vai aparecer automaticamente com as informações do dispositivo em multilinha.
>>>>>>> 95e44dbe5ed37081591c778c9846e31f65bf5c45

---

# Development Process

## 12. Version Control and Commit History
<<<<<<< HEAD

Dado o âmbito reduzido do projeto, o controlo de versões foi gerido de forma simples, com commits na branch `main` organizados segundo a progressão natural do desenvolvimento — primeiro a estrutura de UI e depois a lógica de acesso ao sistema.

## 13. Difficulties and Lessons Learned

A escolha de usar raw strings do Kotlin (`""" """`) com interpolação de variáveis foi deliberada. Inicialmente considerei concatenar com `+` e `\n`, mas rapidamente percebi que ficaria muito mais verboso e difícil de manter. As raw strings permitem escrever o formato final exactamente como vai aparecer no ecrã, com a indentação controlada pelo `trimIndent()` no final.

## 14. Future Improvements

A aplicação atual mostra apenas informações básicas de hardware e versão do sistema. Seria interessante expandir para incluir outras categorias como estado da bateria, informação de rede, espaço de armazenamento disponível ou até memória RAM. Envolver o `TextView` num `ScrollView` tornaria a aplicação escalável para apresentar muito mais dados sem ultrapassar os limites do ecrã.

Do ponto de vista visual, a formatação poderia ser melhorada com secções separadas (Hardware, Software, Network) e uso de negrito ou cores para destacar categorias, em vez de apenas texto monocromático.
=======
O desenvolvimento foi guardado com Git. Os commits seguiram a progressão do trabalho: geração do projeto base, ajuste do XML para suportar texto longo, implementação da leitura dos dados do sistema no `MainActivity.kt`, e pequenas correções de formatação.

## 13. Difficulties and Lessons Learned
No início não sabia bem como aceder às informações do dispositivo — não estava claro por onde começar. Depois de consultar a documentação oficial do Android para developers, percebi que a classe `android.os.Build` era exatamente o que precisava, e que tem atributos estáticos para tudo o que o enunciado pedia. Também aprendi a diferença entre `Build.VERSION.SDK_INT` (o número inteiro da API) e `Build.VERSION.RELEASE` (a versão legível como "14"). A principal lição foi que ler a documentação oficial antes de tentar resolver o problema poupa muito tempo.

## 14. Future Improvements
- Substituir o TextView por uma `RecyclerView` com pares chave-valor, o que ficaria mais legível e organizado.
- Adicionar um botão para copiar as informações para a área de transferência.
>>>>>>> 95e44dbe5ed37081591c778c9846e31f65bf5c45

---

## 15. AI Usage Disclosure
<<<<<<< HEAD

**Código: [AC YES, AI NO]**  

Todo o código foi desenvolvido inteiramente pelo aluno sem recurso a ferramentas de geração de código por inteligência artificial. Apenas o autocomplete nativo do Android Studio foi utilizado.

**Relatório: [AC YES, AI YES]**  

A redação e estruturação deste relatório foi assistida pelo modelo **Claude (Anthropic)**. O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.

---
=======
Usei IA (Claude, da Anthropic) para ajudar a escrever e rever o texto deste README, garantindo que ficasse claro e bem estruturado. Todo o código foi escrito por mim com base na documentação oficial do Android. A IA não foi usada para escrever código.
>>>>>>> 95e44dbe5ed37081591c778c9846e31f65bf5c45

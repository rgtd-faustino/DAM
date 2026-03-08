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

```kotlin
val info = """
    Brand: ${Build.BRAND}
    Model: ${Build.MODEL}
    Manufacturer: ${Build.MANUFACTURER}
    Device: ${Build.DEVICE}
    Android Version: ${Build.VERSION.RELEASE}
    API Level: ${Build.VERSION.SDK_INT}
""".trimIndent()

val systemInfo = findViewById<TextView>(R.id.textSystemInfo)
systemInfo.setText(info)
```

## 5. Testing and Validation
Não tinha um dispositivo físico disponível, por isso testei tudo num emulador Android dentro do Android Studio. Verifiquei que os dados apareciam corretamente — o fabricante aparecia como "Google", o modelo mostrava os metadados do dispositivo virtual, e o texto multilinha renderizava bem, de acordo com a imagem de referência do enunciado.

## 6. Usage Instructions
1. Fazer clone do repositório para o computador.
2. Abrir a pasta raiz do projeto no Android Studio.
3. Aguardar o fim do Gradle Sync.
4. Compilar e executar com o botão Run, com um emulador já iniciado.
5. O ecrã vai aparecer automaticamente com as informações do dispositivo em multilinha.

---

# Development Process

## 12. Version Control and Commit History
O desenvolvimento foi guardado com Git. Os commits seguiram a progressão do trabalho: geração do projeto base, ajuste do XML para suportar texto longo, implementação da leitura dos dados do sistema no `MainActivity.kt`, e pequenas correções de formatação.

## 13. Difficulties and Lessons Learned
No início não sabia bem como aceder às informações do dispositivo — não estava claro por onde começar. Depois de consultar a documentação oficial do Android para developers, percebi que a classe `android.os.Build` era exatamente o que precisava, e que tem atributos estáticos para tudo o que o enunciado pedia. Também aprendi a diferença entre `Build.VERSION.SDK_INT` (o número inteiro da API) e `Build.VERSION.RELEASE` (a versão legível como "14"). A principal lição foi que ler a documentação oficial antes de tentar resolver o problema poupa muito tempo.

## 14. Future Improvements
- Substituir o TextView por uma `RecyclerView` com pares chave-valor, o que ficaria mais legível e organizado.
- Adicionar um botão para copiar as informações para a área de transferência.

---

## 15. AI Usage Disclosure
Usei IA (Claude, da Anthropic) para ajudar a escrever e rever o texto deste README, garantindo que ficasse claro e bem estruturado. Todo o código foi escrito por mim com base na documentação oficial do Android. A IA não foi usada para escrever código.

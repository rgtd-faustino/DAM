# Assignment 4 — Investment Calculator (Hello World)
Course: Desenvolvimento de Aplicações Móveis
Student(s): A51394 Rafael Faustino
Date: 08/03/2026
Repository URL: https://github.com/GameDevRafael/DAM_TP1_HelloWorld

## 1. Introduction
O enunciado pedia para desenvolver uma aplicação Android que fosse além do típico "Hello World". O projeto requeria dois layouts diferentes (Portrait e Landscape), interações com botões, inputs de texto, notificações com Toast, e exploração do Logcat via ADB. O tema escolhido foi uma calculadora de investimentos.

## 2. System Overview
A aplicação calcula o rendimento acumulado de um montante ao longo de vários anos, com duas taxas de juro para comparar:
- **Obrigações**: 3,5% ao ano.
- **Ações**: 8,0% ao ano.

Quando o utilizador carrega em "Calcular", aparecem o lucro e a variação percentual formatados em euros. A app também tem um portfolio simulado que mostra o saldo num Toast e grava um histórico em Logcat para consulta via ADB.

## 3. Architecture and Design
Toda a lógica está na `MainActivity.kt`. A UI usa dois layouts XML:
- `activity_main.xml` — layout Portrait.
- `layout-land/activity_main.xml` — layout Landscape com os controlos da calculadora.

O código verifica em runtime qual a orientação ativa antes de aplicar listeners, para evitar erros com views que só existem num dos layouts.

## 4. Implementation

### Problema com o Edge-to-Edge (Root View)
O Android Studio gerava código que tentava encontrar um elemento com `id="main"` no layout, mas esse id não existia, o que causava erro. A solução foi adicionar `android:id="@+id/main"` manualmente ao contentor raiz de cada XML.

### Radio Buttons para as Taxas
Usei `setOnCheckedChangeListener` nos radio buttons de "Ações" e "Obrigações" para atualizar uma label com a taxa correspondente (ex: "8,0% / ano") sempre que o utilizador alterna entre as duas opções.

### Conversão de Texto para Número (EditText)
Os inputs do utilizador chegam sempre como `String`, mesmo que sejam números. Usei `toDoubleOrNull()` e `toIntOrNull()` para converter os valores e tratar inputs inválidos. Se o campo estiver vazio ou inválido, mostro "Valores inválidos!" e paro a execução.

Uma dificuldade aqui foi que usar `return` dentro do `setOnClickListener` tentava sair do `onCreate` inteiro em vez de sair só do listener. Depois de pesquisar na documentação, descobri os **labeled returns** do Kotlin — a solução foi usar `return@setOnClickListener` para sair apenas do bloco do clique.

### Guardas para Evitar Crashes entre Orientações
Quando o código tenta aceder a uma view que só existe no layout Landscape enquanto está em Portrait, o resultado é null e a app crasha. Para resolver isto, adicionei verificações `if (view == null) return` antes de atribuir qualquer listener, o que resolve o problema sem complicar a lógica.

### Cálculo de Juros Compostos
Para calcular juros compostos, em vez de usar `Math.pow()` do Java, usei a extensão nativa do Kotlin em `Double`:
```kotlin
val total = capital * (1 + taxa).pow(anos.toDouble())
```

### Formatação de Texto (€ e Percentagens)
Usei `String.format()` com `%.2f` para limitar a duas casas decimais. O `%%` é necessário para imprimir o símbolo `%` literalmente sem o motor de formatação o interpretar como um placeholder:
```kotlin
textLucro.text = "Total: %.2f€ (+%.1f%%)".format(total, percentagem)
```

### Logcat e Toasts
Usei `Log.i()` com a tag "Portfolio" para registar os dados do portfolio simulado. Filtrando com `adb logcat -s Portfolio:I` num PC, aparecem apenas esses logs. O botão do portfolio também mostra um Toast com o saldo atual.

## 5. Testing and Validation
- Testei inputs em branco e inválidos para garantir que a app mostra aviso e não crasha.
- Comparei os resultados da calculadora manualmente com os mesmos valores para verificar que a matemática estava correta.
- Testei a rotação do dispositivo para confirmar que as guardas nulas funcionavam e a app não fechava.
- Verifiquei o Toast do portfolio e os logs correspondentes no Logcat.

## 6. Usage Instructions
1. Abrir a aplicação e rodar o dispositivo para Landscape para aceder aos controlos da calculadora.
2. Introduzir o capital inicial e o número de anos.
3. Escolher entre "Ações" (8%) ou "Obrigações" (3,5%) e verificar que a taxa atualiza automaticamente.
4. Carregar em "Calcular" para ver os resultados.
5. Carregar no botão do Portfolio para ver o saldo em Toast e registar os dados no Logcat (filtrável com `adb logcat -s Portfolio:I`).

---

# Development Process

## 12. Version Control and Commit History
O projeto foi gerido com Git no Android Studio. Fiz commits incrementais à medida que cada funcionalidade ficava pronta — layout base, cálculo, tratamento de orientações, portfolio e logs.

## 13. Difficulties and Lessons Learned
- **Guardas nulas para orientações**: Aprendi que ao contrário de uma webpage, uma view que não existe no layout atual devolve null e crasha a app. As verificações `if (x == null) return` resolveram isso.
- **Conversão EditText → Double**: O `toDoubleOrNull()` foi muito mais limpo do que usar `try/catch` para tratar inputs inválidos.
- **Labeled returns**: Não sabia que um `return` simples dentro de um listener saía do `onCreate` inteiro. Pesquisando na documentação do Kotlin encontrei os labeled returns (`return@setOnClickListener`), que resolvem exatamente isso.

## 14. Future Improvements
- Mover os valores fixos (taxas, mensagens) para o `strings.xml` em vez de estarem hardcoded no Kotlin.
- Guardar o estado dos inputs com `onSaveInstanceState` para não se perderem ao rodar o ecrã.
- Ligar o portfolio a uma API REST para buscar cotações reais em vez de usar valores estáticos nos logs.

---

## 15. AI Usage Disclosure
Usei IA (Claude, da Anthropic) para ajudar a escrever e organizar o texto deste README com base nos comentários e código do projeto. Todo o código foi escrito por mim. A IA não foi usada para escrever código.

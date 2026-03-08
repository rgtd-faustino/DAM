# Assignment 1 — Investment Calculator (Hello World)
Course: Desenvolvimento de Aplicações Móveis (DAM)
Student(s): A51394 Rafael Faustino
Date: 2026
Repository URL: https://github.com/GameDevRafael/DAM_TP1

## 1. Introduction
O que era pedido no enunciado: Desenvolver a primeira aplicação Android base da cadeira que vai além do típico "Hello World" num ecrã vazio. O projeto requeria que fossem construídas vistas diferentes para duas orientações (Portrait e Landscape), utilizando interações entre botões, inputs, notificações dinâmicas (Toast) e exploração das saídas de depuração e filtragem através de LogCat (ADB). As diferentes lógicas da App são concentradas com as "Views" centrais da paleta Android no desenvolvimento da App ("Investment Calculator").

## 2. System Overview
A aplicação permite calcular o rendimento acumulado de um certo montante inserido ao longo de vários anos, sujeito a taxas de juro fixas e comparativas de diferentes ativos financeiros previstos:
- **Obrigações**: rendimento de 3,5% ao ano.
- **Ações**: rendimento de 8,0% ao ano.

Quando o utilizador pressiona "Calcular", o lucro individual e a variação da percentagem são apresentados com formatação financeira (€). Além do mais, a aplicação prevê a monitorização constante de um Portfolio simulado de Ações reais que, através da interface, mostra o saldo através de uma notificação emergente no ecrã e grava localmente o histórico discriminado em Logs para posterior auditoria via ADB.

## 3. Architecture and Design
A aplicação é suportada pela lógica central controlada na `MainActivity.kt`.
No que diz respeito ao seu front-end (vistas):
- Utiliza os esquemas nativos XML base do Android, bifurcando o ecrã normal num layout Portrait (`activity_main.xml`).
- Utiliza um ecrã dedicado alternativo Landscape (`layout-land/activity_main.xml`) com as ferramentas e opções da calculadora (onde as vistas e caixas têm propriedades distintas ativas). As validações e escutas em Kotlin determinam quais dos elementos das orientações estão disponíveis na UI daquele instante do ciclo. 

## 4. Implementation

Nesta secção sublinho os processos de reflexão para colmatar necessidades de UI e lógicas implementadas ao longo do Exercício:

### Definição do Root View Edge-to-Edge
Logo no processo de design deparei-me com uma dificuldade trazida pelo motor moderno base que tenta ligar às barras do sistema (bateria / menu inferior). A biblioteca estava a procurar identificar a base do modelo através de `findViewById(R.id.main)` mas recebia erro e não encontrava nenhum elemento com o nome main.
* **Solução**: Atribuí manualmente o identificador explícito `android:id="@+id/main"` diretamente no contentor raiz nativo (LinearLayout/Constraint) de cada XML para resolver o conflito.

### Listener de Tipologia e Juro (Radio Buttons)
Para transitar as taxas visualmente e registá-las para os cálculos, criei `setOnCheckedChangeListener` onde ao alternar entre "Ações" ou "Obrigações", uma label interativa apresenta a taxa equivalente (ex.: "8,0% / ano").

### Transição entre Strings e Números nas Caixas de Texto (EditText)
Os inputs na app embora aceitem visualmente só números são devolvidos sempre em formato `String` nativo do Java/Kotlin.
Para isto tive de criar tratamentos e passagens de tipo nos botões usando as ferramentas standard como `toDoubleOrNull()` e `toIntOrNull()` que também protegem de inputs defeituosos.
Estando vazio, devolvo a quem me usa na etiqueta "Valores inválidos!", informando do que falha.
* *Solução Notável/Labels:* Ao detetar que os valores eram inválidos (nulos) dentro do momento do Clique (`Listener`), um `return` base levava o bloco a querer sair prematuramente do próprio `onCreate()`, finalizando as chamadas. Descobri como os Labeled Returns mitigavam isso na pesquisa à documentação, trocando para: **`return@setOnClickListener`**  e apenas saindo dessa "verificação de segurança".

### Prevenir Crashes entre Modos do Ecrã (Guardas Nulas)
Percebi que desenhando os botões só pro-layout (`Landscape`), se os testasse na orientação em Pé (`Portrait`), os IDs como `editAnos` que o código instanciou à força viajavam como nulos, fazendo com que aplicar propriedades de cliques originasse grandes erros impeditivos de execução, e fechos em loop pela JVM.
**A minha linha de pensamento**: Para a `MainActivity` entender, passei uma instrução `if (view == null)` validando interblocos antes sequer de assinar eventos. Assim tudo rola lisamente face às ausências.

### Cálculo Nativo de Potência (Anotações do IDE e Math)
Houve uma oportunidade para evoluir face a código Java padrão. Na fórmula base `x * Math.pow(...)` para o juro composto, entendi (guiado por auto-completes na ferramenta e documentação) que usar as features embutidas de Double era o core syntax, originando então `(1 + taxa).pow(anos.toDouble())`. 

### Strings e Formatação (€ e Percentagens)
No tratamento textual, li as opções standard para imprimir os formatos monetários sem perdas numéricas longas ("ex: 10.334311..."). Cheguei à combinação base format() recorrendo a literais escapados `%` e restrições de formatações decimais (`%.2f`)
Exemplo:
```kotlin
// Limitacao para duas casas decimais no numero total final, terminando o simbolo de divisa.
textLucro.text = "Total: %.2f€ (+%.1f%%)".format(total, percentagem) 
```
A nota do "scape" `%%` é essencial porque diz ao processador formatação para imprimir esse mesmo símbolo cru em vez de iniciar um pedido de "Placeholder param" para as conversões string/number.

### Logcat ADB + Toasts
Com base no pedido do enunciado para fazer com que os logs filtrassem um Portfolio fictício interativo, agrupei chamadas `Log.i()` estendidas aos tickets e ativadas na UI por Botão de evento num Toast. Quando inspecionados num PC hospedeiro com `adb logcat -s Portfolio:I` devolve todos os dados corretamente isolados no output.

## 5. Testing and Validation
Procedi a testes de usabilidade e fronteira na App:
- Simulei conversões de cliques com dados em branco garantindo que a aplicação avisa gentilmente "Valores Inálidos" e não dispara *Exceções Nuas de Formatos Numéricos*.
- Comparei resultados de matemática para lucro gerado testando na APP Ações + Capital, contra saídas calculadas das minhas mãos com os mesmos parâmetros simulados. 
- Viragem do Telemóvel/Máquina virtual validando que o telemóvel retira do loop de cálculo em Portrait de forma natural graças aos verificadores lógicos de NullPointer Exceptions descritos no Raciocínio (Guardas nulas de IF)
- Verificação cruzada do Toast com logs de info subjacentes nos Debugging tools integrados.

## 6. Usage Instructions
1. Abra e instale a aplicação. Verifique a saudação e vire o Dispositivo à diagonal / Paisagem para iniciar controlos.
2. Escreva o "Capital" nas respetivas caixinhas limadas a dígitos e de seguida "Anos" do investimento tido em conta.
3. Escolha se prefere basear as contas comparativas no Juro Fixo de "Ações" (8%) ou "Obrigações" (3.5%). Verifique se o indicador varia automaticamente.
4. Carregue no Botão Geral Calcular; o Ecrã mostrará detalhadamente retornos com sinal "+" ou o detalhe absoluto entre juros compostos.
5. Em paralelo existe o Menu portfólio. Clique uma vez para ler no Sistema um total apurado imediato, e caso ligado a depurador (Cabo PC Android -> ADB) filtrar localmente todas as ações independentes em Log.I 

# Development Process

## 12. Version Control and Commit History
*Foi usado como sublinhado o Gestor de controlo normal (Git/AS) na qual eu gravei os saltos lógicos com Commits incrementais partilhando em Repository as variações de Lógica e Refactorings (separatórias do modelo e de lógicas entre vistas).* — *A justificar dependendo dos commit tags que tenhas criado nas Push actions do repositório*

## 13. Difficulties and Lessons Learned
- **A gestão nula das Views nos Androids**: Achei bastante crucial os erros de dependência. Ao contrário de uma Webpage, um ecrã na orientação que não exibe aquela tag vai atirar uma exceção pesada ao Kotlin ao ser associado. Aprender Guardas (`if x == null return`) mitigou o cenário mas aumentou as lições sobre os Activity Lifecycles.
- **Tipagem Dinâmica entre EditBox -> String -> Double**: Relembrei as falhas nos tipos estáticos e utilizei abordagens mais limpas da JVM. A capacidade de um auto-fallback, como o método `toDoubleOrNull()` revelaram ser ferramentas excecionais para proteção face a inputs indesejados sem usar complexas "try/catches".
- **Diferença Clara de Returns em Escadas de Eventos**: Compreender a profundidade de eventos. Sair do evento Click através do `return` fechava a Class inteira de Init. Aprendi (através de referências cruzadas sobre Labeled Returns da Google Docs) a devolver apenas retorno local à `onClickListener()`.

## 14. Future Improvements
- Passar os controláveis ("Valores de Avisos e Retornos numéricos") que estão "HardCoded/fixos" em ficheiros puros Java/Kotlin e transladá-los ordenadamente para ficheiro centralizado da Linguagens na APP como dita a boa forma : _`strings.xml`_.
- Adicionar uma salvaguarda (Através do override do método nativo `onSaveInstanceState`). Atualmente as variáveis inseridas de "Anos e Cap" seriam efémeras com qualquer rotação/reset do Landscape se ativadas bruscamente (Uma vez que a view é redesenhada). Seria implementado suporte a `Bundle` States no futuro onde isso seja mais central. 
- Criar funções utilitárias unidas como o Botão de Portfolio mas acoplados ao tráfego assíncrono (Networking e HTTPs API Restfuls) puxando cotação em tempo livre sem "Log" estático. 

## 15. AI Usage Disclosure
Na realização deste primeiro grande módulo, a inteligência artificial teve propósitos isolados e delimitados de aprendizagem, sem substituição na arquitetura. Concretamente:
- **Ferramentas de IDE (IntelliSense/Lint)**: Foram usados para recomendações dinâmicas de passagem nos métodos nativos de Java para Kotlin, exemplo, a passagem da library estática Math `Math.pow` para o facilitador orgânico de Double do próprio runtime Kotlin `.pow(...)`. 
- **Assistência em Elaboração de Documentação**: Utilizei o assistente para compilar todos os meus fragmentos de conhecimento criados em comentários fonte da App em torno do projeto para redigir, formatar profissionalmente e alicerçar as secções deste ficheiro explicativo, respeitando a integridade das explicações exatas e originais que desenvolvi para o projeto submetido. Evidenciando uma escrita superior que comunica devidamente uma explicação limpa para um projeto académico sem adulterar o que codifiquei propriamente.

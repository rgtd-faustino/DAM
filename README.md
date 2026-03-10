# Assignment 3 — Hello World V2

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 10/03/2026  
**Repository URL:** https://github.com/GameDevRafael/DAM_TP1_HelloWorld

---

## 1. Introdução

Este exercício corresponde à secção 4.2 do Tutorial 1 de DAM e constitui a evolução directa do Hello World V1. Enquanto a versão anterior se limitava a apresentar uma mensagem de texto estática, a versão 2 introduz uma interface mais rica, com múltiplos componentes visuais e uma organização de layout baseada em `ConstraintLayout`.

O exercício 4.2 serviu de base para consolidar conhecimentos sobre posicionamento de vistas através de constraints, utilização de recursos tipados em `strings.xml`, integração de imagens com `ImageView` e incorporação de um `CalendarView`. Paralelamente, a aplicação foi expandida no modo landscape com uma interface temática de dashboard financeira, demonstrando como o Android permite layouts completamente distintos por orientação.

## 2. Visão Geral do Sistema

### Modo Portrait

O layout portrait (`res/layout/activity_main.xml`) apresenta quatro componentes dispostos verticalmente:

| Componente | ID | Descrição |
|---|---|---|
| `TextView` | `textView6` | Cabeçalho roxo com o nome da aplicação ("Hello Word V2") em 34sp |
| `TextView` | `textView` | Saudação "Hello Android World!" em verde, 30sp, negrito |
| `FrameLayout` + `ImageView` | `frameLayout` / `imageView` | Contém um emoji facial (`emoji.jpg`) carregado via `srcCompat` |
| `TextView` | `textView3` | Barra verde com o texto "My First App" em 20sp |
| `CalendarView` | `calendarView` | Calendário nativo do Android posicionado abaixo dos componentes anteriores |

### Modo Landscape

O layout landscape (`res/layout-land/activity_main.xml`) é uma interface temática completamente diferente — uma **Bíblia Financeira** — dividida em três colunas com `LinearLayout` horizontal e peso igual (`layout_weight="1"`):

- **Coluna esquerda:** Portfólio com cinco activos (VWCE, TTWO, NVDA, BTC, Gold), valores actuais e variações percentuais com código de cor (verde `#00ffb3` para positivo, vermelho `#ff4d6d` para negativo)
- **Coluna central:** Histórico de transações com datas, tipo de operação (COMPRA/VENDA) e activo transaccionado; botão "VER PORTFÓLIO"
- **Coluna direita:** Calculadora de lucro com juros compostos, dois RadioButtons (Obrigações 3,5% / Ações 8,0%), campos de capital e anos, e botão "CALCULAR"

## 3. Arquitetura e Design

### Estrutura do Projeto

```
app/src/main/
├── java/dam_a51394/helloworld/
│   └── MainActivity.kt          # Lógica da actividade
├── res/
│   ├── drawable/
│   │   └── emoji.jpg            # Imagem do emoji
│   ├── font/
│   │   └── syne.xml             # Fonte personalizada (landscape)
│   ├── layout/
│   │   └── activity_main.xml    # Layout portrait (exercício 4.2)
│   ├── layout-land/
│   │   └── activity_main.xml    # Layout landscape (dashboard financeira)
│   └── values/
│       ├── strings.xml          # Todos os textos da aplicação
│       ├── themes.xml           # Tema Material3 DayNight NoActionBar
│       └── colors.xml
```

**Ícone da aplicação:**  
O ícone foi personalizado para reflectir a temática financeira da aplicação. Representa um livro com um gráfico de barras ascendente e uma seta de crescimento, simbolizando investimentos e valorização de ativos.

### Decisões de Design

**Portrait — ConstraintLayout com hierarquia top-to-bottom:**  
Cada componente é posicionado relativamente ao anterior através de `layout_constraintTop_toBottomOf`, criando uma cadeia vertical. O `textView6` ancora-se ao topo do ecrã (`constraintTop_toTopOf="parent"`), e os restantes componentes descem em cascata. Esta abordagem mantém o posicionamento estável independentemente do tamanho do ecrã.

O `FrameLayout` foi utilizado como contentor para o `ImageView` para garantir controlo preciso das dimensões (312×92dp) sem depender do conteúdo da imagem. O `ImageView` preenche o `FrameLayout` com `match_parent` em ambas as dimensões.

**Landscape — LinearLayout horizontal com três ConstraintLayouts:**  
A divisão em três colunas de peso igual simplifica a distribuição do espaço disponível em modo landscape. Cada coluna é um `ConstraintLayout` independente, o que permite posicionamento flexível dentro de cada secção sem interferir com as restantes.

A paleta de cores do landscape (`#121221` como fundo, `#00ffb3` para valores positivos, `#ff4d6d` para negativos) foi escolhida para simular o aspecto de uma aplicação financeira real. A fonte `Syne` confere consistência tipográfica a toda a secção landscape.

**Gestão de strings:**  
Todos os textos visíveis, incluindo valores numéricos estáticos (e.g. `_1_250`, `_615_13`), foram declarados em `strings.xml`. Esta decisão segue as boas práticas Android e facilita eventual localização futura, embora alguns identificadores numéricos sejam pouco descritivos — aspecto reconhecido como área de melhoria.

## 4. Implementação

### Layout Portrait — Componentes Principais

**Cabeçalho com fundo colorido:**
```xml
<TextView
    android:id="@+id/textView6"
    android:layout_width="0dp"
    android:layout_height="80dp"
    android:background="#673AB7"
    android:text="@string/app_name"
    android:textColor="#FFFFFF"
    android:textSize="34sp"
    android:textStyle="bold"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

A largura `0dp` em combinação com `constraintStart` e `constraintEnd` ancorados ao `parent` faz com que o `TextView` ocupe toda a largura disponível — padrão comum para elementos de cabeçalho em `ConstraintLayout`.

**ImageView dentro de FrameLayout:**
```xml
<FrameLayout
    android:id="@+id/frameLayout"
    android:layout_width="312dp"
    android:layout_height="92dp"
    app:layout_constraintTop_toBottomOf="@+id/textView3">

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:contentDescription="@string/face_emoji"
        app:srcCompat="@drawable/emoji" />
</FrameLayout>
```

O atributo `contentDescription` foi preenchido com referência a `strings.xml`, cumprindo os requisitos de acessibilidade Android. O atributo `srcCompat` (em vez de `src`) garante compatibilidade com versões anteriores do Android através da biblioteca AppCompat.

**CalendarView:**
```xml
<CalendarView
    android:id="@+id/calendarView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginStart="31dp"
    android:layout_marginTop="28dp"
    android:layout_marginEnd="31dp"
    app:layout_constraintTop_toBottomOf="@+id/frameLayout" />
```

O `CalendarView` foi colocado no final da hierarquia vertical, ancorado à base do `FrameLayout`. As margens laterais de 31dp proporcionam espaçamento visual adequado. O `CalendarView` não tem lógica de selecção implementada — funciona como componente visual estático nesta fase do exercício.

### Lógica da Calculadora (MainActivity.kt)

A `MainActivity` detecta o modo de orientação verificando a existência dos componentes do landscape antes de registar listeners, evitando `NullPointerException` em portrait:

```kotlin
if (editCapital == null || editAnos == null || ...)
    return
```

O cálculo de juros compostos aplica a fórmula `M = C × (1 + i)^n`:

```kotlin
val total = capital * (1 + taxa).pow(anos.toDouble())
val lucro = total - capital
val percentagem = (lucro / capital) * 100

textResultado.text = "+%.2f€".format(lucro)
textLucro.text = "Total: %.2f€ (+%.1f%%)".format(total, percentagem)
```

A formatação `%%` no template de string produz um único `%` literal — necessário porque `format()` interpreta `%` como marcador de substituição.

Os `RadioButton` actualizam dinamicamente o `TextView` de taxa ao mudar de seleção:

```kotlin
radioObrigacoes.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) textTaxa.text = "3,5% / ano"
}
radioAcoes.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) textTaxa.text = "8,0% / ano"
}
```

O botão "VER PORTFÓLIO" exibe um `Toast` com o total do portfólio. Durante o desenvolvimento verificou-se que o segundo `setOnClickListener` do mesmo botão substitui o primeiro — o listener de `Log.i` ficou assim inactivo, sendo substituído pelo `Toast`.

### Ciclo de Vida e Edge-to-Edge

A aplicação utiliza `enableEdgeToEdge()` e aplica `WindowInsetsCompat` para gerir o padding em dispositivos com barras de sistema, garantindo que o conteúdo não fica obstruído pela barra de estado ou de navegação.

O comentário no código documenta uma dificuldade encontrada com o `ViewCompat.setOnApplyWindowInsetsListener`: o elemento raiz necessitou do `id="main"` para que o sistema conseguisse localizar a vista-raiz — problema que ocorre quando o layout raiz não tem ID explícito e o sistema não encontra nenhuma vista com esse nome.

## 5. Testes e Validação

### Validação em Emulador

A aplicação foi testada em emulador Android com validação das duas orientações:

**Portrait:**
- Todos os componentes visuais (cabeçalho roxo, texto verde, emoji, barra verde, calendário) renderizam correctamente
- O `CalendarView` exibe o mês actual por omissão
- O `enableEdgeToEdge()` garante que o cabeçalho não fica encoberto pela barra de estado do sistema

**Landscape:**
- A transição de orientação carrega o layout alternativo em `layout-land/`
- As três colunas dividem o ecrã com peso igual
- A calculadora produz resultados correctos; testado com capital=1000€, anos=10, Ações (8%) → resultado de aproximadamente +1.158,93€ de lucro
- A validação de campos vazios (capital ou anos nulos) retorna "Valores inválidos" sem crash
- O `Toast` do portfólio apresenta-se no ecrã por tempo curto (`LENGTH_SHORT`)

### Verificação de Constraints

Os constraints do portrait foram verificados manualmente no editor visual do Android Studio. O `horizontal_bias` de alguns componentes landscape foi ajustado iterativamente para obter o alinhamento visual pretendido — processo típico quando se trabalha com posicionamento preciso em `ConstraintLayout`.

## 6. Instruções de Utilização

### Pré-requisitos

- Android Studio (versão Hedgehog ou superior recomendada)
- SDK Android API 24 ou superior
- Kotlin 1.9+

### Clonar e Configurar

```bash
git clone https://github.com/GameDevRafael/DAM_TP1_HelloWorld.git
cd DAM_TP1_HelloWorld
```

1. Abrir o Android Studio
2. Seleccionar **File → Open** e navegar até à pasta do projecto
3. Aguardar a sincronização do Gradle (`Build → Sync Project with Gradle Files`)
4. Seleccionar um emulador ou dispositivo físico na barra de ferramentas
5. Executar com **Run → Run 'app'** (ou `Shift+F10`)

Para testar o modo landscape no emulador, rodar o dispositivo virtual com o botão de rotação na barra lateral do emulador.

### Utilização do ADB (Android Debug Bridge)

O ADB foi utilizado para interagir com o emulador durante o desenvolvimento. As ferramentas encontram-se em:
```bash
cd C:\Users\galve\AppData\Local\Android\Sdk\platform-tools
```

Comandos principais utilizados:
```bash

# Listar dispositivos conectados
./adb devices

# Monitorizar logs específicos em tempo real
./adb logcat -s Portfolio
./adb logcat -s Calcular

# Capturar screenshot do emulador
./adb shell screencap /sdcard/screenshot.png
./adb pull /sdcard/screenshot.png C:\Users\galve\Desktop\screenshot.png

# Verificar resolução do ecrã
./adb shell wm size
```

---

# Development Process

## 12. Version Control and Commit History

O repositório segue uma estratégia de commits por funcionalidade concluída. Os commits principais cobrem: criação do projecto base, implementação do layout portrait (exercício 4.2), desenvolvimento do layout landscape com a dashboard financeira, e integração da lógica Kotlin na `MainActivity`.

O repositório mantém uma única branch `main`, adequada para um projecto de dimensão individual com âmbito académico.

## 13. Difficulties and Lessons Learned

**Fontes personalizadas no Android:**
A tentativa de utilizar a fonte `Syne` directamente no campo `fontFamily` falhou com o erro
`Cannot resolve symbol: 'syne'`. O Android não reconhece fontes externas sem as importar
explicitamente. A solução foi aceder a **More Fonts** no editor de atributos, que faz o
download e configura automaticamente a pasta `res/font/`.

**Layouts e posicionamento livre:**
A primeira tentativa de posicionamento utilizou `FrameLayout`, que não é adequado para
dividir o ecrã — serve para sobrepor elementos. Para dividir o ecrã em colunas a solução
correcta é um `LinearLayout` horizontal com `layout_weight="1"` em cada filho. Dentro de
cada coluna foi utilizado `ConstraintLayout` para posicionamento livre dos elementos.

**O atributo `layout_weight` exige `layout_width="0dp"`:**
Ao utilizar `layout_weight` num `LinearLayout` horizontal, o `layout_width` de cada filho
tem de ser `0dp` — sem isso o peso é ignorado e as colunas não dividem o espaço correctamente.

**ID obrigatório na vista raiz do layout landscape:**
O `ViewCompat.setOnApplyWindowInsetsListener` procura a vista raiz pelo `id="main"`.
O layout landscape não tinha esse ID definido, causando `NullPointerException` na linha 20
do `onCreate`. A solução foi adicionar `android:id="@+id/main"` ao `ConstraintLayout` raiz
do ficheiro `layout-land/activity_main.xml`.


## 14. Future Improvements

- **Persistência do CalendarView:** Implementar um listener `setOnDateChangeListener` no `CalendarView` para guardar e exibir a data seleccionada, tornando o componente interactivo.
- **Toast vs. Log consolidado:** Unificar o listener do botão de portfólio para registar simultaneamente no Logcat e apresentar o `Toast`, eliminando a substituição de listeners.
- **Calculadora com validação mais robusta:** Adicionar validação de valores negativos e zero no capital e nos anos, com feedback mais descritivo ao utilizador.
- **Strings numéricas com nomenclatura descritiva:** Renomear os recursos de string com identificadores numéricos (e.g. `_615_13`) para nomes de domínio (e.g. `vwce_price`) para melhorar a legibilidade do código.
- **Modo portrait expandido:** Integrar parte da funcionalidade da calculadora no portrait, aproveitando o espaço abaixo do `CalendarView`.
- **Tema unificado:** Definir a paleta de cores do landscape no `colors.xml` e referenciá-la a partir de ambos os layouts, eliminando valores hexadecimais repetidos inline.

---

## 15. AI Usage Disclosure

**Código: [AC YES, AI NO]**  
Todo o código foi desenvolvido inteiramente pelo aluno sem recurso a ferramentas de geração de código por inteligência artificial. Apenas o autocomplete nativo do Android Studio foi utilizado.

**Relatório: [AC YES, AI YES]**  
A redação e estruturação deste relatório foi assistida pelo modelo **Claude (Anthropic)**. O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.
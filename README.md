# Assignment 1 — Kotlin

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 13/03/2026  
**Repository URL:** [DAM_TP1](https://github.com/rgtd-faustino/DAM_TP1)

---

## 1. Introdução

Este relatório descreve o desenvolvimento dos exercícios de Kotlin da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM) do ISEL. O objetivo principal deste trabalho foi ganhar familiaridade com a linguagem Kotlin, explorando as suas funcionalidades básicas e intermédias num contexto prático, antes de avançar para o desenvolvimento Android propriamente dito.

O projeto foi desenvolvido como uma aplicação Kotlin/Maven organizada em quatro packages independentes, cada um focado num tema ou conjunto de conceitos específicos: arrays e funções, calculadoras com tratamento de exceções, sequências funcionais e modelação orientada a objetos.

---

## 2. Visão Geral do Sistema

O projeto `DAM_TP1` está estruturado como um projeto Kotlin com Maven e contém quatro exercícios distintos:

- **`dam.exer_1`** — Geração de arrays de quadrados perfeitos utilizando três abordagens diferentes.
- **`dam.exer_2`** — Calculadora interativa com suporte a operações aritméticas, lógicas e de deslocamento de bits.
- **`dam.exer_3`** — Cálculo de alturas de ressalto de uma bola utilizando `generateSequence`.
- **`dam.exer_vl`** — Sistema de biblioteca virtual com hierarquia de classes, membros e operações de empréstimo.

Cada package tem o seu próprio `main()` e pode ser executado de forma independente.

---

## 3. Arquitetura e Design

### Exercício 1 — Arrays de Quadrados Perfeitos

Foram implementados três métodos distintos para gerar um array com os primeiros 50 quadrados perfeitos:

1. Usando o construtor `IntArray(n) { lambda }`.
2. Usando um `range` com `map {}` e conversão para `IntArray`.
3. Usando o construtor `Array<Int>(n) { lambda }`.

A escolha de três abordagens distintas foi intencional para demonstrar a flexibilidade do Kotlin na manipulação de coleções e arrays.

### Exercício 2 — Calculadora

A calculadora foi implementada com um loop `while` que mantém o programa em execução até o utilizador introduzir `"sair"`. A estrutura `when` foi usada para despachar as operações, e o tratamento de exceções com `try/catch` foi aplicado na divisão por zero.

A validação dos inputs é feita com ciclos `while(true)` com `break`, garantindo que o programa não avança com dados inválidos.

### Exercício 3 — Bounce Heights

A sequência de alturas de ressalto é gerada com `generateSequence`. A primeira abordagem passava por uma função auxiliar `bounceBall()` separada, responsável por calcular a nova altura e retornar `null` quando esta ficasse abaixo do mínimo:
```kotlin
fun bounceBall(previousHeight: Float, newHeightPercent: Float, minHeight: Float): Float? {
    val newHeight = previousHeight * newHeightPercent
    return if (newHeight >= minHeight) newHeight else null
}
```

Após consultar melhor a documentação do Kotlin, percebeu-se que `takeIf` permite condensar esta lógica diretamente no lambda do `generateSequence`, eliminando a função auxiliar por completo:
```kotlin
val bounces = generateSequence(currentHeight) { previousHeight ->
    (previousHeight * newHeightPercent).takeIf { it >= minHeight }
}
```

Esta abordagem é mais idiomática em Kotlin e reduz significativamente a verbosidade do código sem perder clareza.

### Exercício VL — Virtual Library

A hierarquia de classes foi desenhada da seguinte forma:
```
Book (abstract)
├── DigitalBook
└── PhysicalBook
```

Algumas decisões de design merecem destaque:

- **`publicationYear` como propriedade calculada vs. getter:** Em Kotlin não é possível alterar o tipo de retorno num getter relativamente à propriedade base. Por isso, `publicationYear` foi declarado como uma propriedade regular com valor atribuído no `init`, em vez de tentar usar um getter com tipo diferente.

- **Aviso de "out of stock" em `decreaseAvailableCopies()`:** O aviso de stock esgotado foi colocado no método `decreaseAvailableCopies()` e não no `setter` de `availableCopies`. Esta separação mantém o setter focado na validação de dados (garantir que o valor não é negativo), enquanto o aviso que o livro ficou sem cópias pertence ao método que executa a ação.

- **`toString()` na classe base com `getStorageInfo()` abstrato nas subclasses:** Em vez de fazer override de `toString()` em cada subclasse (como o enunciado sugeria), optou-se por implementar `toString()` apenas em `Book`, delegando a parte específica de cada tipo para o método abstrato `getStorageInfo()`. Esta abordagem evita duplicação de código e é mais alinhada com os princípios de reutilização, ainda que tecnicamente o enunciado pedisse o override em cada subclasse.

---

## 4. Implementação

### Exercício 1

Os três métodos estão implementados no `main()` do package `dam.exer_1`. A utilização de `replace()` em string templates foi explorada para formatar a saída.

### Exercício 2

As operações estão separadas em funções top-level, ou seja, fora da classe (`sum`, `subtract`, `multiply`, `divide`, `or`, `and`, `not`, `shiftLeft`, `shiftRight`). A função `divideCheck()` lança uma `IllegalArgumentException` quando o divisor é zero, e o bloco `try/catch` na função principal trata essa situação.

O operador `!` (not) é o único unário (apenas requer um operando), pelo que o fluxo de execução termina antes de pedir o segundo operando quando este operador é selecionado.

### Exercício 3

A lógica é compacta e está toda contida no `main()`. A altura inicial é 100f, o fator de ressalto é 60%, a altura mínima é 1f, e são mostrados no máximo os primeiros 15 ressaltos. A formatação da saída usa `"%.2f".format(bounce)` para apresentar duas casas decimais.

### Exercício VL

As classes implementadas são:

| Classe | Tipo | Descrição |
|---|---|---|
| `Book` | `abstract class` | Classe base com propriedades comuns e métodos de empréstimo |
| `DigitalBook` | `class` | Extensão com `fileSize` e `format` |
| `PhysicalBook` | `class` | Extensão com `weight` e `hasHardcover` |
| `Library` | `class` | Gere a lista de livros com operações de pesquisa e empréstimo |
| `LibraryMember` | `data class` | Representa um membro com lista de livros emprestados |

O `companion object` na classe `Library` mantém o contador estático `totalBooks` partilhado entre todas as instâncias.

---

## 5. Testes e Validação

Os testes foram realizados manualmente através da execução direta de cada `main()`. Foram validados os seguintes cenários:

- **Exercício 1:** Verificação visual de que os 50 quadrados perfeitos estão corretos (1, 4, 9, ..., 2500) nos três métodos.
- **Exercício 2:** Testes com inputs inválidos (texto em vez de número, operadores não permitidos), divisão por zero, e todas as operações disponíveis.
- **Exercício 3:** Verificação de que a sequência termina corretamente quando a altura desce abaixo de 1f, e que o limite de 15 ressaltos é respeitado.
- **Exercício VL:** Validação do empréstimo quando não há cópias disponíveis, devolução de livros, pesquisa por autor, e aviso de out of stock.

---

## 6. Instruções de Utilização

### Pré-requisitos

- JDK 17 ou superior
- Maven 3.8+
- IntelliJ IDEA (recomendado)

### Executar o projeto

Clonar o repositório:
```bash
git clone https://github.com/GameDevRafael/DAM_TP1
cd DAM_TP1
```

Compilar com Maven:
```bash
mvn compile
```

Para executar cada exercício, navegar até ao respetivo `main()` no IntelliJ e usar o botão de execução, ou configurar o `exec-maven-plugin` com a classe principal desejada.

### Estrutura de packages
```
src/main/kotlin/org/example/dam/
├── exer_1/        # Arrays de quadrados perfeitos
├── exer_2/        # Calculadora interativa
├── exer_3/        # Bounce heights com generateSequence
└── exer_vl/       # Virtual Library
    ├── Book.kt
    ├── DigitalBook.kt
    ├── PhysicalBook.kt
    ├── Library.kt
    ├── LibraryMember.kt
    └── Main.kt
```

---

# Autonomous Software Engineering Sections

> As secções 7 a 11 não se aplicam a este trabalho. Todo o código foi desenvolvido com **[AC NO, AI NO]**, pelo que não houve recurso a ferramentas de geração de código por IA. A IA foi utilizada apenas na redação do relatório (ver secção 15).

---

# Development Process

## 12. Version Control and Commit History

Os commits foram feitos de forma incremental, por exercício, de modo a refletir a progressão do desenvolvimento. Cada commit corresponde tipicamente à conclusão ou refinamento de um exercício específico.

---

## 13. Difficulties and Lessons Learned

- **`field` vs `this.property` nos setters:** Dentro de um setter, tentar atribuir valor usando `this.availableCopies = value` gerava um aviso de redundância. A solução correta em Kotlin é usar `field = value`, que referencia o backing field da propriedade.

- **Refatoração do Exercício 3:** A implementação inicial usava uma função auxiliar `bounceBall()` separada para tratar a terminação da sequência. Após rever a documentação, percebeu-se que `takeIf` resolve o mesmo problema de forma muito mais concisa diretamente no lambda do `generateSequence`, dispensando a função auxiliar e tornando o código mais idiomático em Kotlin.

- **Hierarquia de classes e `toString()`:** Compreender quando faz sentido centralizar lógica na classe base em vez de repetir código nas subclasses foi um exercício útil de design orientado a objetos.

- **Nullable types e o operador `?`:** O uso de `Book?` na `Library` foi necessário para inicializar a variável como `null` antes do ciclo de pesquisa. Sem o `?`, o compilador não permite atribuição posterior de `null`.

---

## 14. Future Improvements

- **Exercício 2:** Adicionar suporte a expressões encadeadas (e.g., `3 + 4 * 2`) em vez de apenas operações binárias simples.
- **Exercício VL:** Implementar pesquisa por título além de por autor, e associar os livros emprestados diretamente aos membros da `LibraryMember`.
- **Exercício VL:** Persistência dos dados em ficheiro, para que o estado da biblioteca não se perca ao terminar a execução.

---

## 15. AI Usage Disclosure

**Código: [AC NO, AI NO]**  
Todo o código foi desenvolvido inteiramente pelo aluno sem recurso a ferramentas de autocomplete assistido por IA nem a ferramentas de geração de código por inteligência artificial. Todo o código e comentários foram produzidos manualmente.

**Relatório: [AC YES, AI YES]**  
A redação e estruturação deste relatório foi assistida pelo modelo **Claude (Anthropic)**. O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.

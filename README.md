# Assignment 1 — Kotlin Fundamentals

Course: Desenvolvimento de Aplicações Móveis (DAM)
Student(s): A51394 Rafael Faustino
Date: 2026
Repository URL: https://github.com/GameDevRafael/DAM_TP1

---

## 1. Introduction

Este trabalho prático tem como objetivo consolidar os fundamentos da linguagem Kotlin através de quatro exercícios independentes. Cada exercício aborda um conjunto diferente de conceitos: arrays e lambdas, operações aritméticas e lógicas, sequências funcionais, e programação orientada a objetos com herança e encapsulamento. O enunciado pedia que cada exercício fosse implementado num package próprio dentro do mesmo projeto Kotlin.

---

## 2. System Overview

O projeto é composto por quatro módulos independentes:

- **exer_1** — Geração de arrays com os quadrados dos números de 1 a 50, usando três abordagens distintas.
- **exer_2** — Calculadora interativa com suporte a operadores aritméticos, lógicos e de bit shift.
- **exer_3** — Simulação de ressalto de bola usando `generateSequence` e programação funcional.
- **exer_vl** — Sistema de biblioteca com livros digitais e físicos, herança, getters/setters e companion objects.

Todos os módulos correm em linha de comandos (console) sem interface gráfica.

---

## 3. Architecture and Design

### exer_1
Três abordagens para construir um `IntArray` com os quadrados de 1 a 50:
1. `IntArray(50) { i -> ... }` com índice explícito.
2. Range `(1..50).map { it * it }` convertida para array.
3. `Array(50) { i -> ... }` com tipo genérico.

### exer_2
Funções top-level separadas por operação (`sum`, `subtract`, `divide`, etc.). O fluxo principal em `calculadoraUsar()` valida inputs em loop antes de os usar, evitando crashes por input inválido.

### exer_3
Uso de `generateSequence` para modelar a sequência de ressaltos. A função `bounceBall` retorna `Float?` — quando a altura cai abaixo do mínimo, retorna `null`, terminando a sequência naturalmente.

### exer_vl
Hierarquia de classes:
- `Book` (abstract) → `DigitalBook`, `PhysicalBook`
- `Library` com `companion object` para estado partilhado
- `LibraryMember` como `data class`

---

## 4. Implementation

### exer_1 — Arrays e Lambdas
O enunciado pedia três formas de criar um array com os quadrados de 1 a 50. A principal dificuldade foi perceber que o índice do `IntArray` começa em 0, logo foi necessário fazer `i + 1` para obter a base correta. Aprendi a distinção entre usar `i` (quando se declara uma variável lambda antes da seta `->`) e `it` (quando não se declara):

```kotlin
val array1 = IntArray(50) { i -> val base = i + 1; base * base }
val lista = (1..50).map { it * it }
```

Para o output usei `.replace()` com um placeholder, o que tornou o código mais reutilizável.

### exer_2 — Calculadora
O enunciado pedia suporte a `+`, `-`, `*`, `/`, `||`, `&&`, `!`, `shl`, `shr`, com validação de inputs. A lógica de validação foi colocada em loops `while(true)` com `break` quando o input é válido. Para a divisão por zero, optei por lançar uma `IllegalArgumentException` dentro de `divideCheck()` e capturá-la com `try/catch` no `when`. O operador `!` não precisa de segundo valor, por isso tem um `return` antecipado antes de pedir o segundo operando:

```kotlin
if (operador == "!") {
    val x = not(primeiroValor!!)
    print("!$primeiroValor: $x")
    return
}
```

### exer_3 — Sequência de Ressaltos
O enunciado pedia simular ressaltos de bola usando programação funcional. Usei `generateSequence` com um lambda que multiplica a altura anterior por `newHeightPercent`. Quando a altura fica abaixo de `minHeight`, `bounceBall` retorna `null`, o que termina a sequência. `.take(15)` limita o output aos primeiros 15 ressaltos válidos.

### exer_vl — Biblioteca OOP
O enunciado pedia uma hierarquia de classes com herança, getters/setters e companion object. A maior dificuldade foi o setter de `availableCopies`: ao tentar escrever `availableCopies = value` dentro do setter, o compilador assinalava um aviso. Consultei a documentação e percebi que dentro de um setter se deve usar `field` para referenciar o backing field:

```kotlin
var availableCopies: Int = availableCopiesGetter
    set(value) {
        if (value >= 0) field = value
    }
```

O `toString()` foi implementado na classe mãe `Book`, pois as subclasses partilham a mesma estrutura base. A informação específica de cada tipo de livro foi isolada em `getStorageInfo()`, que é `abstract` e obrigatoriamente implementada pelas subclasses.

---

## 5. Testing and Validation

Os testes foram feitos manualmente correndo cada `main()` e verificando o output. Para o **exer_2**, testei casos de erro (input não numérico, operador inválido, divisão por zero) para garantir que os loops de validação funcionavam corretamente. Para o **exer_vl**, testei `borrowBook` com mais pedidos do que cópias disponíveis e `returnBook` após esgotar o stock.

---

## 6. Usage Instructions

1. Abrir o projeto no IntelliJ IDEA.
2. Navegar até ao package do exercício pretendido (ex: `org.example.dam.exer_2`).
3. Executar a função `main()` do ficheiro correspondente.
4. Para o **exer_2**, seguir as instruções no terminal (introduzir números e operadores quando pedido; escrever `sair` para terminar).

---

# Development Process

## 12. Version Control and Commit History

O repositório foi gerido com Git através do IntelliJ IDEA. Os commits foram organizados por exercício, fazendo commit após cada exercício estar funcional e testado. O repositório está disponível em: `https://github.com/GameDevRafael/DAM_TP1`

---

## 13. Difficulties and Lessons Learned

- **Índices em lambdas (exer_1):** Perceber que `IntArray` começa em índice 0 e que `it` vs `i` depende de se declaramos ou não uma variável lambda explícita.
- **Backing field (exer_vl):** O uso de `field` dentro de um setter foi uma descoberta importante — sem ele, o setter entraria em recursão infinita.
- **`generateSequence` com null (exer_3):** Compreender que retornar `null` num lambda de `generateSequence` é a forma idiomática de terminar a sequência em Kotlin.
- **`!!` vs `?.` (exer_2):** Aprendi a diferença entre o operador `!!` (força não-nulo, lança exceção se nulo) e `?.` (safe call). Usei `!!` nos pontos onde a validação anterior já garantia que o valor não era nulo.

---

## 14. Future Improvements

- **exer_2:** Adicionar histórico de operações e suporte a expressões compostas (ex: `3 + 4 * 2`).
- **exer_vl:** Implementar persistência (guardar a biblioteca em ficheiro JSON), associar `LibraryMember` ao sistema de empréstimos, e adicionar pesquisa por era ou ano.
- **exer_3:** Tornar os parâmetros configuráveis via input do utilizador em vez de valores fixos.

---

## 15. AI Usage Disclosure

O assistente de IA (Claude, da Anthropic) foi utilizado para gerar a estrutura e o texto deste README com base no código fornecido, nos comentários presentes no código e nas instruções do estudante. Todo o código dos exercícios foi escrito pelo estudante. A IA não foi usada para resolver os exercícios nem para escrever código — apenas para apoio na documentação escrita.
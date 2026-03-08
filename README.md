# Assignment 1 — Kotlin Fundamentals

Course: Desenvolvimento de Aplicações Móveis (DAM)
Student(s): A51394 Rafael Faustino
Date: 2026
Repository URL: https://github.com/GameDevRafael/DAM_TP1

---

## 1. Introduction

Trabalho prático com quatro exercícios de consola em Kotlin, com o objetivo de consolidar fundamentos da linguagem antes de avançar para desenvolvimento Android: arrays e coleções, validação de input, sequências lazy e programação orientada a objetos.

---

## 2. System Overview

| Exercício | Pacote | Descrição |
|---|---|---|
| Exer 1 | `exer_1` | Três formas de gerar um array com os quadrados de 1 a 50 |
| Exer 2 | `exer_2` | Calculadora interativa com validação de input e múltiplos operadores |
| Exer 3 | `exer_3` | Simulação de ressaltos de bola com `generateSequence` |
| Exer VL | `exer_vl` | Sistema de biblioteca com herança (`Book`, `DigitalBook`, `PhysicalBook`) |

---

## 3. Implementation

### Exercício 1 — Arrays
Implementadas três abordagens para o mesmo resultado: `IntArray` com lambda indexado, range `(1..50)` com `map`, e `Array` genérico. O principal aprendizado foi a distinção entre `it` (parâmetro lambda implícito, sem `->`) e `i` (parâmetro explícito, com `->`), e perceber que os índices começam em 0, logo é necessário `i + 1`.

### Exercício 2 — Calculadora
As operações foram separadas em funções puras (`sum`, `subtract`, etc.), mantendo o `main()` limpo. A validação usa `toFloatOrNull()` em loops até receber input válido. A divisão por zero lança uma `IllegalArgumentException` capturada com `try-catch`. O operador `!` é tratado antes de pedir o segundo valor, pois não o requer.

```kotlin
fun divideCheck(b: Float): Boolean {
    return if (b == 0f) throw IllegalArgumentException("Não podes dividir por 0!") else true
}
```

### Exercício 3 — Bounce Ball
Usada `generateSequence` para gerar uma sequência lazy que termina quando `bounceBall` retorna `null` (altura abaixo do mínimo). O `take(15)` recolhe os primeiros 15 ressaltos válidos sem materializar a sequência inteira.

```kotlin
val bounces = generateSequence(currentHeight) { prev ->
    bounceBall(prev, newHeightPercent, minHeight)
}
```

### Exercício VL — Biblioteca
Hierarquia `Book → DigitalBook / PhysicalBook`, com a classe `Library` a gerir uma lista mutável. Os métodos `borrowBook` e `returnBook` verificam disponibilidade de cópias antes de alterar o estado. Em Kotlin as classes são `final` por defeito, pelo que foi necessário marcá-las com `open` para permitir herança.

---

## 4. Testing and Validation

Testes manuais em todos os exercícios. Pontos verificados:
- **Exer 1:** Primeiro elemento 1 (1²), último 2500 (50²), nas três abordagens.
- **Exer 2:** Input inválido → programa pede novamente; divisão por zero → mensagem de erro sem crash; `sair` → termina o programa.
- **Exer 3:** Exatamente 15 ressaltos impressos, valores a decrescer a partir de 100.
- **Exer VL:** Empréstimo sem cópias disponíveis falha corretamente; devolução incrementa as cópias; pesquisa por autor filtra corretamente.

---

## 5. Usage Instructions

1. Clonar o repositório e abrir no IntelliJ IDEA como projeto Kotlin/Gradle.
2. Navegar até ao `main.kt` do exercício desejado e executar a função `main()`.
3. No Exercício 2 (interativo): introduzir os valores e operador quando pedido. Para sair, escrever `sair` no primeiro prompt.

---

## 6. Difficulties and Lessons Learned

- **`it` vs parâmetro explícito:** `it` só está disponível quando o parâmetro do lambda não é declarado explicitamente — levou algum tempo a interiorizar.
- **Nullable types:** O compilador forçar o tratamento de `null` pareceu burocrático no início, mas previne NullPointerExceptions que seriam silenciosas em Java.
- **Sequências lazy:** `generateSequence` não avalia elementos imediatamente, só quando consumidos — conceito contra-intuitivo mas poderoso.
- **Código duplicado na calculadora:** A validação de operadores ficou repetida antes e dentro do loop. Deveria ter sido extraída para uma função auxiliar.

---

## 7. Future Improvements

- Extrair a validação de input da calculadora para funções reutilizáveis.
- Adicionar modo interativo ao Exercício 3 (configurar altura e percentagem em runtime).
- Adicionar persistência à biblioteca (ficheiro JSON) e uma interface Android.
- Escrever testes unitários com JUnit para os casos limite de cada exercício.

---

## 8. AI Usage Disclosure

Foi utilizado o Claude (Anthropic) para esclarecer dúvidas conceptuais sobre `generateSequence`, nullable types e lambdas, e para a redação deste README. Todo o código foi escrito e compreendido pelo estudante — a IA foi usada como ferramenta de apoio, não como substituto da implementação.
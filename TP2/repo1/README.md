# Assignment 1 — Kotlin

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 12/04/2026  
**Repository URL:** [DAM_TP2_Kotlin](https://github.com/rgtd-faustino/DAM/edit/main/TP2/repo1/)

---

## 1. Introdução

Este relatório descreve o desenvolvimento do segundo conjunto de exercícios de Kotlin da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM). Após a introdução inicial à linguagem, este trabalho foca-se em conceitos mais avançados e idiomáticos do Kotlin, explorando funcionalidades que permitem escrever código mais seguro, expressivo e funcional.

O projeto foi estruturado para demonstrar a proficiência na gestão de hierarquias de tipos com `sealed classes`, a implementação de estruturas de dados genéricas, a construção de componentes de processamento funcional (pipelines) e a modelagem matemática através de sobrecarga de operadores. Toda a implementação segue as convenções oficiais do Kotlin e tira partido de funções de extensão para manter o código modular e limpo.

---

## 2. Visão Geral do Sistema

O projeto `DAM_TP2_Kotlin` está organizado em quatro exercícios principais, cada um abordando um domínio técnico específico dentro do ecossistema Kotlin:

- **`dam.exer_1`** — Sistema de gestão de eventos de utilizador (`Login`, `Purchase`, `Logout`) utilizando classes seladas e funções de extensão para análise de dados.
- **`dam.exer_2`** — Implementação de uma `Cache` genérica com suporte a transformações funcionais, filtragem e snapshots imutáveis.
- **`dam.exer_3`** — Motor de processamento `Pipeline` para transformações em cadeias de strings, com suporte a composição de etapas e bifurcação (*fork*).
- **`dam.exer_4`** — Modelação de um vetor bidimensional (`Vec2`) com implementação extensiva de operadores aritméticos e de comparação.

---

## 3. Arquitetura e Design

### Exercício 1 — Gestão de Eventos (Sealed Classes)

A escolha de uma `sealed class` para representar os `Event` foi fundamental para garantir a segurança de tipos e permitir que o compilador verifique a exaustividade nos blocos `when`.
A arquitetura baseia-se em:
- **Hierarquia Selada:** `Login`, `Purchase` e `Logout` partilham a base `Event`.
- **Extensões de Coleções:** Em vez de poluir as classes de dados com lógica de negócio, as operações de filtragem e cálculo de totais foram implementadas como funções de extensão de `List<Event>`.
- **Functional Handlers:** A função `processEvents` utiliza uma *High-Order Function* para delegar o processamento de cada evento, permitindo desacoplar a iteração da lógica de execução.

### Exercício 2 — Cache Genérica

O componente `Cache<K, V>` foi desenhado para ser versátil e tipificado. Decisões de design relevantes:
- **Transformação In-Place:** O método `transform` permite atualizar um valor existente através de um lambda, garantindo a atomicidade lógica da alteração.
- **Imutabilidade em Snapshots:** Ao exportar o estado da cache (`snapshot()`), o sistema retorna uma cópia imutável para evitar efeitos secundários indesejados fora da classe.
- **Segurança de Nulos:** Utilização cautelosa de `!!` em contextos onde a existência da chave já foi validada (por exemplo, dentro do `transform` após o `contains`).

### Exercício 3 — Pipeline de Processamento

O `Pipeline` foi implementado utilizando um dicionário (`MutableMap`) para mapear nomes de estágios a funções de transformação (`(List<String>) -> List<String>`).
- **Composição de Estágios:** O método `compose` funde dois estágios existentes numa nova função composta, reduzindo a complexidade de execução ao transformar duas chamadas separadas numa única cadeia.
- **Estrutura DSL:** A função `buildPipeline` permite uma inicialização declarativa, facilitando a construção de pipelines complexos de forma legível.

### Exercício 4 — Modelação Matemática (Vec2)

O `Vec2` foi implementado como uma `data class` para beneficiar de métodos gerados automaticamente (`equals`, `hashCode`, `toString`).
- **Sobrecarga de Operadores:** Foram implementados operadores para adição (`+`), subtração (`-`), multiplicação escalar (`*`) e negação unária (`-`).
- **Comparação por Magnitude:** A interface `Comparable` foi implementada para permitir ordenar vetores com base no seu comprimento (magnitude euclidiana).
- **Acesso por Índice:** O operador `get` permite aceder às coordenadas `x` e `y` utilizando a sintaxe de array (`v[0]`, `v[1]`), melhorando a ergonomia da classe.

---

## 4. Implementação

### Exercício 1

As operações de agregação estão implementadas como extensões:
```kotlin
fun List<Event>.totalSpent(parameter: String): Double {
    var total = 0.0
    for(i in 0 .. this.size - 1) {
        if ((this.get(i) is Purchase) && (this.get(i) as Purchase).username == parameter)
            total += (this.get(i) as Purchase).amount
    }
    return total
}
```

### Exercício 2

A classe `Cache` gere o estado interno através de um `MutableMap` privado:

| Método | Descrição |
|---|---|
| `getOrPut` | Retorna o valor existente ou insere um valor padrão via lambda |
| `transform` | Aplica uma função de transformação ao valor associado a uma chave |
| `filterValues` | Retorna uma nova vista da cache com base num predicado |

### Exercício 3

A lógica de composição demonstra o poder das funções de primeira classe em Kotlin:
```kotlin
fun compose(stageName1: String, stageName2: String) {
    val steps1 = stepsList[stageName1]!!
    val steps2 = stepsList[stageName2]!!
    stepsList.remove(stageName1)
    stepsList.remove(stageName2)
    stepsList[stageName1 + "_" + stageName2] = { input -> steps2(steps1(input)) }
}
```

### Exercício 4

Implementação da sobrecarga de operadores e tratamento de erros na normalização:
```kotlin
fun normalized(): Vec2 {
    val mag = magnitude()
    if (mag == 0.0) throw IllegalStateException("Não podes normalizar um vetor (0, 0)")
    return Vec2(x / mag, y / mag)
}
```

---

## 5. Testes e Validação

A validação foi efetuada através da execução de programas `main()` de teste em cada package:

- **Exercício 1:** Validado o cálculo de gastos totais para diferentes utilizadores e a filtragem correta de sequências de eventos.
- **Exercício 2:** Testada a persistência da cache, o comportamento do `getOrPut` e a imutabilidade dos snapshots produzidos.
- **Exercício 3:** Verificada a execução sequencial de transformações de texto e a correta união de estados em estágios compostos.
- **Exercício 4:** Validados cálculos vetoriais manuais (soma, produto interno, magnitude) e o comportamento da ordenação em listas de vetores.

---

## 6. Instruções de Utilização

### Pré-requisitos

- Java JDK 17+
- Maven 3.8+
- Kotlin 1.9+

### Execução

Compilar o projeto:
```bash
mvn compile
```

Correr os testes de integração (se disponíveis):
```bash
mvn test
```

Para executar um exercício específico, utilize o plugin exec do Maven ou execute diretamente a classe `MainKt` do respetivo package no seu IDE.

---

# Autonomous Software Engineering Sections

> As secções subsequentes detalham o processo de desenvolvimento e as escolhas técnicas efetuadas durante a realização do trabalho.

---

# Development Process

## 12. Version Control and Commit History

O desenvolvimento seguiu uma abordagem incremental, com commits organizados por exercício. Esta metodologia permitiu manter o histórico limpo e facilitar a identificação de eventuais regressões durante a refactorização dos componentes mais complexos (como o `Pipeline`).

---

## 13. Difficulties and Lessons Learned

- **Destructuring e Data Classes:** Durante a implementação do `Vec2`, houve uma tentativa inicial de implementar manualmente as funções `component1()` e `component2()`. No entanto, descobriu-se que as `data classes` já geram estas funções automaticamente, causando conflitos de assinatura. A lição aprendida foi confiar mais nas automatizações nativas do Kotlin para tipos de dados simples.
- **DSL e Lambdas com Receptor:** A construção do `buildPipeline` permitiu compreender melhor como o Kotlin facilita a criação de mini-linguagens específicas para configuração de objetos.
- **Gestão de Nulos em Genéricos:** No exercício da `Cache`, a utilização de `!!` foi um ponto de reflexão. Embora desencorajado em código de produção geral, revelou-se a forma pragmática de lidar com o compilador quando a verificação lógica prévia já garantia a presença do valor no mapa.

---

## 14. Future Improvements

- **Persistência de Cache:** Implementar uma camada de serialização para permitir que a cache sobreviva a reinícios da aplicação.
- **Operadores Vec2 Expandidos:** Adicionar suporte para coordenadas 3D e operações matriciais básicas.
- **Pipeline Async:** Evoluir o motor de processamento para suportar estágios assíncronos (Coroutines) para lidar com operações de I/O pesadas.

---

## 15. AI Usage Disclosure

**Código: [AC NO, AI NO]**  
Todo o código foi desenvolvido inteiramente pelo aluno sem recurso a ferramentas de autocomplete assistido por IA nem a 
ferramentas de geração de código por inteligência artificial. Todo o código e comentários foram produzidos manualmente.

**Relatório: [AC YES, AI YES]**  
A estruturação e redação final deste relatório foram assistidas pelo modelo **Antigravity**, garantindo a conformidade com o formato académico exigido, mantendo a autenticidade técnica dos conteúdos descritos.

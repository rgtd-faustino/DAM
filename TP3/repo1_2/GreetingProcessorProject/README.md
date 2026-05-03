# Assignment 1 & 2 — Annotation Processor

**Course:** Desenvolvimento de Aplicações Móveis (DAM)  
**Student:** A51394 Rafael Faustino  
**Date:** 03/05/2026  
**Repository URL:** [DAM_TP3_Kotlin](https://github.com/rgtd-faustino/DAM/tree/main/TP3/repo1_2/GreetingProcessorProject)

---

## 1. Introdução

Este relatório descreve o desenvolvimento dos exercícios de anotações e processamento em tempo de compilação da unidade curricular de Desenvolvimento de Aplicações Móveis (DAM) do ISEL. O objetivo foi perceber como funcionam os annotation processors em Kotlin, nomeadamente como gerar código automaticamente em tempo de compilação para evitar repetição e tornar o código mais declarativo.

O projeto foi implementado como um projeto multi-módulo em Kotlin/JVM com Gradle, dividido em três módulos independentes: `annotations`, `processor` e `app`. O módulo `annotations` define as anotações, o módulo `processor` contém os processadores que geram código novo, e o módulo `app` tem as classes anotadas e o ponto de entrada da aplicação.

Foram implementados dois processadores:

- **`GreetingProcessor`** — Para cada classe com métodos anotados com `@Greeting`, gera uma classe *wrapper* por composição que imprime uma mensagem antes de chamar o método original.
- **`RegexProcessor`** — Para classes com métodos abstratos anotados com `@Extract`, gera uma subclasse concreta que implementa esses métodos com lógica de extração via expressões regulares.

---

## 2. Visão Geral do Sistema

O projeto está organizado em três módulos com responsabilidades separadas:

- **`annotations`** — Define `@Greeting` e `@Extract`, ambas com retenção `SOURCE`, ou seja, só existem em tempo de compilação.
- **`processor`** — Contém `GreetingProcessor` e `RegexProcessor`, que lêem as anotações e geram ficheiros `.kt` novos via KotlinPoet.
- **`app`** — Contém `MyClass`, `DataProcessor` e `Main`, onde as classes geradas são usadas diretamente.

O fluxo é o seguinte:

1. O compilador encontra anotações `@Greeting` ou `@Extract` no código.
2. Invoca o processador registado via `@AutoService` para essas anotações.
3. O processador gera ficheiros `.kt` no diretório `kapt.kotlin.generated`.
4. O código gerado fica disponível para uso imediato no módulo `app`.

---

## 3. Arquitetura e Design

### Exercício 1 — Greeting Annotation Processor

A anotação `@Greeting` foi definida com `@Target(AnnotationTarget.FUNCTION)`, para garantir que só pode ser aplicada a funções, e com `@Retention(AnnotationRetention.SOURCE)` porque não é necessária em runtime, serve apenas para o compilador a processar.

O `GreetingProcessor` usa `@AutoService` para se registar automaticamente no classpath, sem precisar de criar manualmente o ficheiro `META-INF/services`. Sem isto, o compilador ignora o processador por completo.

Os métodos são agrupados por classe num `classMethodMap` antes de gerar qualquer código. Como podem existir vários métodos `@Greeting` na mesma classe, isto garante que é gerada apenas uma classe *wrapper* por classe e não uma por método.

A classe gerada usa composição: recebe a instância original no construtor e delega as chamadas após imprimir a mensagem.

```kotlin
public final class MyClassWrapper(
    public val original: MyClass,
) {
    public final fun sayHello() {
        println("Hello from MyClass!")
        original.sayHello()
    }

    public final fun compute() {
        println("Welcome to the compute function!")
        original.compute()
    }
}
```

O método `process()` retorna `true` no final para indicar que as anotações já foram tratadas e não precisam de ser processadas por mais nenhum processador.

### Exercício 2 — Regex Annotation Processor

A anotação `@Extract` foi adicionada ao mesmo ficheiro `Greeting.kt` do módulo `annotations`. O seu único parâmetro é `regex: String`, a expressão regular que define o que extrair do input.

O `RegexProcessor` segue a mesma lógica de recolha de métodos do `GreetingProcessor`: percorre os elementos anotados com `@Extract`, filtra os `ExecutableElement` e agrupa-os por classe. A diferença está na geração do código.

Aqui não foi possível usar composição como no exercício anterior porque `DataProcessor` é `abstract` e não pode ser instanciada. A classe gerada tem de estender `DataProcessor` e implementar os seus métodos abstratos, passando o `input` ao construtor da superclasse.

```kotlin
public class DataProcessorExtract(
    input: String,
) : DataProcessor(input) {
    override fun getName(): String? {
        val match = Regex("Name: (\\w+)").find(input)
        return match?.groupValues?.get(1)
    }

    override fun getAddress(): String? {
        val match = Regex("Address: (.+)").find(input)
        return match?.groupValues?.get(1)
    }
}
```

Quando o Kotlin faz `Regex("Name: (\\w+)").find("Name: John")`, o resultado guarda dois valores em `groupValues`:
- `groupValues[0]` = `"Name: John"` (a correspondência completa)
- `groupValues[1]` = `"John"` (apenas o conteúdo dentro dos parênteses)

Por isso o acesso é sempre com `.get(1)`. O tipo de retorno é `String?` porque `Regex.find()` devolve `null` se não houver correspondência.

---

## 4. Implementação

### Anotações

As duas anotações estão no mesmo ficheiro `Greeting.kt` no módulo `annotations`:

| Anotação | Parâmetro | Propósito |
|---|---|---|
| `@Greeting` | `message: String` | Mensagem a imprimir antes de chamar o método original |
| `@Extract` | `regex: String` | Expressão regular para extrair dados do input |

Ambas têm `@Retention(AnnotationRetention.SOURCE)`, o que significa que não existem em bytecode nem em runtime e são consumidas exclusivamente pelo compilador.

### GreetingProcessor

Os métodos anotados são recolhidos e agrupados em `classMethodMap`:

```kotlin
for (element in roundEnv.getElementsAnnotatedWith(Greeting::class.java)) {
    if (element is ExecutableElement) {
        val enclosingClass = element.enclosingElement as TypeElement
        classMethodMap.computeIfAbsent(enclosingClass) { mutableListOf() }.add(element)
    }
}
```

Para cada entrada no mapa, é gerada uma classe *wrapper* com KotlinPoet. O `%S` no `addStatement` é um placeholder de string que escapa as aspas automaticamente, o que é necessário porque sem isto o código gerado ficaria mal formatado.

```kotlin
val methodBuilder = FunSpec.builder(methodName)
    .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
    .addParameters(parameters)
    .addStatement("println(%S)", greetingMessage)
    .addStatement("original.$methodName($arguments)")
```

O ficheiro gerado é escrito no diretório `kapt.kotlin.generated`, lido a partir das opções do `processingEnv`.

### RegexProcessor

A recolha de elementos é igual ao `GreetingProcessor`. A geração da classe usa `superclass` e `addSuperclassConstructorParameter` para expressar a herança:

```kotlin
val classBuilder = TypeSpec.classBuilder(wrapperClassName)
    .primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter("input", String::class.asTypeName())
            .build()
    )
    .superclass(ClassName(packageName, originalClassName))
    .addSuperclassConstructorParameter("input")
    .addModifiers(KModifier.PUBLIC) // sem FINAL porque pode ser extendida
```

Cada método gerado usa `KModifier.OVERRIDE` em vez de `KModifier.FINAL`, que é o correto para implementação de métodos abstratos.

### Classes do módulo app

| Ficheiro | Tipo | Descrição |
|---|---|---|
| `MyClass.kt` | `open class` | Dois métodos anotados com `@Greeting` |
| `DataProcessor.kt` | `abstract class` | Dois métodos abstratos anotados com `@Extract` |
| `Main.kt` | ficheiro | Usa `MyClassWrapper` e `DataProcessorExtract` gerados automaticamente |

---

## 5. Testes e Validação

A validação foi feita manualmente através da execução do `main()` no módulo `app`.

- **Exercício 1:** Confirmado que `wrappedMyClass.sayHello()` imprime `"Hello from MyClass!"` antes de `"Executing sayHello method"`, e `wrappedMyClass.compute()` imprime `"Welcome to the compute function!"` antes de `"Computing something important..."`.

- **Exercício 2:** Com o input `"Name: John Address: 123 Street"`, confirmado que `extractor.getName()` devolve `"John"` e `extractor.getAddress()` devolve `"123 Street"`. Testado também com um input sem correspondência, onde o retorno é `null` sem qualquer exceção.

A saída esperada do `main()` é:

```
Hello from MyClass!
Executing sayHello method
Welcome to the compute function!
Computing something important...
Name: John
Address: 123 Street
```

---

## 6. Instruções de Utilização

### Pré-requisitos

- JDK 20 ou superior
- Gradle 8+
- IntelliJ IDEA (recomendado)

### Configuração do projeto

Clonar o repositório:
```bash
git clone https://github.com/rgtd-faustino/DAM
cd DAM/TP3/repo1
```

Garantir que o ficheiro `gradle.properties` contém:
```
kapt.include.compile.classpath=false
```

E que o `settings.gradle.kts` inclui os três módulos:
```kotlin
rootProject.name = "GreetingProcessorProject"
include("annotations")
include("processor")
include("app")
```

### Compilar e executar

```bash
./gradlew build
./gradlew :app:run
```

O código gerado após compilação fica em:
```
app/build/generated/source/kaptKotlin/<package>/
```

### Estrutura de módulos

```
GreetingProcessorProject/
├── annotations/
│   └── src/main/kotlin/annotations/
│       └── Greeting.kt        # @Greeting e @Extract
├── processor/
│   └── src/main/kotlin/processor/
│       ├── processor.kt       # GreetingProcessor
│       └── RegexProcessor.kt  # RegexProcessor
└── app/
    └── src/main/kotlin/app/
        ├── MyClass.kt         # Classe anotada com @Greeting
        ├── DataProcessor.kt   # Classe abstrata anotada com @Extract
        └── Main.kt            # Ponto de entrada
```

---

# Autonomous Software Engineering Sections

> As secções 7 a 11 não se aplicam a este trabalho. Todo o código foi desenvolvido com **[AC NO, AI NO]**...

---

# Development Process

## 12. Version Control and Commit History

Os commits foram feitos de forma incremental, primeiro o `GreetingProcessor` e depois o `RegexProcessor`. Isto permitiu validar o primeiro exercício antes de avançar para o segundo e tornou mais fácil isolar erros durante o desenvolvimento.

---

## 13. Difficulties and Lessons Learned

- **Registo do processor com `@AutoService`:** Não era claro como o compilador descobria os processadores. Percebeu-se que sem o `@AutoService` (que gera automaticamente o ficheiro `META-INF/services/javax.annotation.processing.Processor`) o compilador ignora o processador por completo, sem dar qualquer erro.

- **Composição vs. herança no `RegexProcessor`:** A primeira tentativa foi usar o mesmo padrão de composição do `GreetingProcessor`, com uma propriedade `original` do tipo `DataProcessor`. Só depois se percebeu que `DataProcessor` é `abstract` e não pode ser instanciada, o que torna a composição impossível. A solução foi gerar uma classe que estende `DataProcessor` e passa o `input` ao construtor da superclasse.

- **`groupValues[0]` vs. `groupValues[1]`:** Nos primeiros testes, os métodos devolviam a frase completa (`"Name: John"`) em vez do valor capturado (`"John"`). Consultando a documentação, percebeu-se que `groupValues[0]` é sempre a correspondência completa e `groupValues[1]` é o primeiro grupo de captura, ou seja, o conteúdo dentro dos parênteses do regex.

- **`@SupportedSourceVersion` e a versão do JDK:** A anotação com `SourceVersion.RELEASE_23` dava erros de compilação. Descendo para `SourceVersion.RELEASE_20` o problema desapareceu.

- **`KModifier.FINAL` no `RegexProcessor`:** No `GreetingProcessor` a classe gerada é `final` porque não faz sentido ser extendida. No `RegexProcessor` esse modificador foi omitido porque a classe gerada já é uma subclasse e pode ser reutilizada noutros contextos.

---

## 14. Future Improvements

- **`@Greeting` com suporte a classes:** Estender o `GreetingProcessor` para `@Target(AnnotationTarget.CLASS)`, aplicando a mensagem a todos os métodos públicos da classe sem precisar de anotar cada um individualmente.
- **`@Extract` com múltiplos grupos de captura:** Suportar vários grupos no mesmo regex, devolvendo `List<String?>` em vez de um único `String?`.
- **Validação do regex em tempo de compilação:** Verificar no `RegexProcessor` se o regex é sintaticamente válido e emitir um erro de compilação com `Diagnostic.Kind.ERROR` se não for, em vez de falhar em runtime.
- **Sufixo das classes geradas configurável:** Permitir que o sufixo (`Wrapper`, `Extract`) seja definido como parâmetro da anotação.

---

## 15. AI Usage Disclosure

**Código: [AC NO, AI NO]**  
Todo o código foi desenvolvido pelo aluno sem recurso a ferramentas de autocomplete assistido por IA nem a geração de código por inteligência artificial. Todo o código e comentários foram escritos manualmente.

**Relatório: [AC YES, AI YES]**  
A redação e estruturação deste relatório foram assistidas pelo modelo **Claude (Anthropic)**. O aluno é totalmente responsável pelo conteúdo apresentado e confirma que o mesmo reflete com rigor o trabalho desenvolvido.
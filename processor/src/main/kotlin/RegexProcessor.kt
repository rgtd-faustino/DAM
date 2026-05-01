package processor

import annotations.Extract
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_20)
@SupportedAnnotationTypes("annotations.Extract")
class RegexProcessor : AbstractProcessor() {

    // isto é a mesma lógica que o greeting processor, apenas apanhamos as funções todas que tenham Extract
    // e depois guardamos e dizemos true para que se saiba que já foram processadas
    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {

        val classMethodMap = mutableMapOf<TypeElement, MutableList<ExecutableElement>>()

        for (element in roundEnv.getElementsAnnotatedWith(Extract::class.java)) {
            if (element is ExecutableElement) {
                val enclosingClass = element.enclosingElement as TypeElement
                classMethodMap.computeIfAbsent(enclosingClass) { mutableListOf() }.add(element)
            }
        }

        for ((classElement, methods) in classMethodMap) {
            generateExtractorClass(classElement, methods)
        }

        return true
    }

    // este código é que é diferente do greeting porque o propósito deste processor é diferente
    private fun generateExtractorClass(
        classElement: TypeElement,
        methods: List<ExecutableElement>
    ) {
        // fiz igual porque isto é so para criar a classe, faz me sentido ser o mesmo modo de criação
        val packageName = processingEnv.elementUtils.getPackageOf(classElement).toString()
        val originalClassName = classElement.simpleName.toString()
        // afinal é suposot acabar num "Extract"
        val wrapperClassName = "${originalClassName}Extract"


        // o pensamento aqui é que precisamos de criar a classe à mesma então o código não diverge muito
        // ERRADO: não tinha reparado que o DataProcessor era abstrato portanto não podemos criar um objeto dela
        // ou seja criar o "original" não funciona nem faz sentido
        val classBuilder = TypeSpec.classBuilder(wrapperClassName)
            // metemos o input como parametro da classe
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    // porque o input é uma string
                    .addParameter("input", String::class.asTypeName())
                    .build()
            )/*
            .addProperty(
                PropertySpec.builder("original", ClassName(packageName, originalClassName))
                    .initializer("original")
                    .build()
            )*/
            // adicionamos o extends para a classe DataProcessor
            .superclass(ClassName(packageName, originalClassName)).addSuperclassConstructorParameter("input")
            .addModifiers(KModifier.PUBLIC) // não queremos final


        // aqui diverge mais porque o objetivo do método é definido aqui
        for (method in methods) {
            val methodName = method.simpleName.toString()
            val parameters = method.parameters.map { param ->
                ParameterSpec.builder(param.simpleName.toString(), param.asType().asTypeName()).build()
            }

            // aqui é que muda o que vamos fazer
            // portanto usamos a anotação extract e em vez de apanhar a message apanhamos o regex que queremos
            // adicionalmente como o greeting tinha um backup fez me sentido fazer o nome a ser o backup
            // porque assim conseguimos identificar a pessoa
            val extractRegex = method.getAnnotation(Extract::class.java)?.regex ?: "Name: (\\w+)"

            val methodBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE) // só queremos override
                .addParameters(parameters)
                // pelo que entendi o extractRegex vai substituir o %S e assim vai ficar igual
                // ao código gerado que queremos
                .addStatement("val match = Regex(%S).find(input)", extractRegex)
                .addStatement("return match?.groupValues?.get(1)")
                // isto faz com que seja esperado o return de uma string e o nullable é para aparecer
                // o "?" porque pode ser null
                .returns(String::class.asTypeName().copy(nullable = true))

            classBuilder.addFunction(methodBuilder.build())
        }

        // a partir daqui volta a ser igual porque temos de associar ao pacakge correto e nome do ficheiro
        val file = FileSpec.builder(packageName, wrapperClassName)
            .addType(classBuilder.build())
            .build()

        try {
            val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
            if (kaptKotlinGeneratedDir != null) {
                file.writeTo(File(kaptKotlinGeneratedDir))
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "kapt.kotlin.generated not found"
                )
            }
        } catch (e: Exception) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Error generating Kotlin file: ${e.message}"
            )
        }
    }
}
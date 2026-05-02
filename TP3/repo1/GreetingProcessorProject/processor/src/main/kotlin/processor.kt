package processor

import annotations.Greeting
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

// isto gera automaticamente um ficheiro que é usado para o java descobrir processors em tempo de compilação e sem isto
// o compilador simplesmente não sabe que este processor existe
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_20) // como o 23 dava erro fui descendo até um release que não desse erro
// isto diz ao processor quais as anotações que ele deve processar, o compilador só o vai chamar quando encontrar
// @Greeting no código, senão o processor seria chamado para todas as anotações e isso não é eficiente
@SupportedAnnotationTypes("annotations.Greeting")


// abstract processor é usado para os annotations processors (é a interface entre o código e o compilador)
class GreetingProcessor : AbstractProcessor() {

    // isto é chamado sempre que há codigo novo porque poderá ter anotações novas que têm de ser processadas
    override fun process(
        annotations: MutableSet<out TypeElement>, // conjunto de anotações encontradas
        roundEnv: RoundEnvironment // ambiente da ronda atual quando o código foi adicionado
    ): Boolean {

        // como podem haver vários métodos @Greeting na mesma classe usamos isto para gerar apenas uma wrapper por
        // classe e não por método
        val classMethodMap = mutableMapOf<TypeElement, MutableList<ExecutableElement>>() // lista de método anotados

        // agora andamos por todos os elementos do código que têm @greeting (classe/função/propriedade)
        for (element in roundEnv.getElementsAnnotatedWith(Greeting::class.java)) {
            // mas só queremos ver funções então usamos exectuable event
            if (element is ExecutableElement) {
                // este é o elemento pai ou seja a classe que tem o método anotado
                val enclosingClass = element.enclosingElement as TypeElement
                // computeIfAbsent cria uma lista vazia para esta classe se ainda não existir no mapa
                // e depois adiciona o método à lista
                classMethodMap.computeIfAbsent(enclosingClass) { mutableListOf() }.add(element)
            }
        }

        // para cada classe que tem métodos anotados gera uma wrapper class
        for ((classElement, methods) in classMethodMap) {
            generateKotlinWrapperClass(classElement, methods)
        }

        // isto é só para dizer que este processor já tratou destas anotações então outros processors não precisam
        // das processar novamente
        return true
    }

    private fun generateKotlinWrapperClass(
        classElement: TypeElement,
        methods: List<ExecutableElement>
    ) {
        // aqui usamos elementUtils para obter o nome do package da classe original
        val packageName = processingEnv.elementUtils.getPackageOf(classElement).toString()
        val originalClassName = classElement.simpleName.toString()
        // o nome da classe gerada é o nome da original + "Wrapper" por isso é que MyClass ficou MyClassWrapper
        val wrapperClassName = "${originalClassName}Wrapper"

        // aqui estamos a construir a class MyClassWrapper(val original: MyClass)
        val classBuilder = TypeSpec.classBuilder(wrapperClassName)
            // isto define o construtor primário da classe gerada com o parâmetro "original" do tipo da classe original
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("original", ClassName(packageName, originalClassName))
                    .build()
            )
            // adiciona a propriedade "original" à classe e liga-a ao parâmetro do construtor
            // sem o initializer("original") teríamos o parâmetro no construtor mas não como propriedade acessível
            .addProperty(
                PropertySpec.builder("original", ClassName(packageName, originalClassName))
                    .initializer("original")
                    .build()
            )
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)

        for (method in methods) {
            val methodName = method.simpleName.toString()
            // converte os parâmetros do método original para ParameterSpec para os poder adicionar
            // ao método gerado com os tipos corretos
            val parameters = method.parameters.map { param ->
                ParameterSpec.builder(param.simpleName.toString(), param.asType().asTypeName()).build()
            }
            // isto serve para construir a string de argumentos para passar ao método original
            // por exemplo se o método tem (name: String, age: Int), arguments fica "name, age"
            val arguments = method.parameters.joinToString(", ") { it.simpleName.toString() }
            // lemos o valor do atributo "message" da anotação @Greeting no método e se não existir
            // usa "Hello!" como backup
            val greetingMessage = method.getAnnotation(Greeting::class.java)?.message ?: "Hello!"

            // %S é um placeholder de string que escapa automaticamente as aspas porque estamos
            // a gerar código que contém strings e sem isto o código gerado ficaria mal formatado
            val methodBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addParameters(parameters)
                .addStatement("println(%S)", greetingMessage) // imprime a mensagem antes de chamar o original
                .addStatement("original.$methodName($arguments)") // passa a chamada para o método original

            classBuilder.addFunction(methodBuilder.build())
        }

        // associa a classe gerada ao package correto e ao nome do ficheiro
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
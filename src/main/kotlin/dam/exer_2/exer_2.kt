package org.example.dam.exer_2

fun sum(a: Float, b: Float): Float = a + b
fun subtract(a: Float, b: Float): Float = a - b
fun multiply(a: Float, b: Float): Float = a * b

fun divideCheck(b: Float): Boolean {
    // se tentarmos dividir por 0 mandamos uma exceção a dizer que não é possível
    return if (b == 0f) {
        throw IllegalArgumentException("Não podes dividir por 0!")
    } else
        true
}

fun divide(a: Float, b: Float): Float = a / b
fun or(a: Float, b: Float): Boolean = (a != 0f) || (b != 0f)
fun and(a: Float, b: Float): Boolean = (a != 0f) && (b != 0f)
fun not(a: Float): Boolean = a == 0f
fun shiftLeft(a: Float, b: Float): Int = a.toInt() shl b.toInt()
fun shiftRight(a: Float, b: Float): Int = a.toInt() shr b.toInt()

var usarPrograma = "usar" // mantém o código em loop até o utilizador quiser sair

fun main() {
    println("Isto é uma calculadora")
    while (usarPrograma == "usar")
        calculadoraUsar()
}

fun calculadoraUsar() {
    println("Se desejar sair da calculadora introduza a palavra: sair")
    print("Introduz o primeiro valor desejado: ")

    val input = readln()

    // se o utilizador desejar sair da calculadora só tem de introduzir "sair" e o loop pára
    if (input == "sair") {
        usarPrograma = "sair"
        return
    }

    // se continuarmos a introduzir valores que não são números isto volta a pedir
    var primeiroValor = input.toFloatOrNull()

    if (primeiroValor == null) {
        while (true) {
            print("Tens de introduzir um número: ")
            primeiroValor = readln().toFloatOrNull()

            if (primeiroValor != null)
                break
        }
    }

    // temos de introduzir um operador dos listados para isto seguir em frente em vez de continuar a pedir
    print("Introduz o operador desejado (+, -, /, *, &&, ||, !, shl, shr): ")
    var operador = readln()

    if (operador != "+" && operador != "-" && operador != "/" && operador != "*" &&
        operador != "||" && operador != "&&" && operador != "!" &&
        operador != "shl" && operador != "shr"
    ) {
        while (true) {
            println("Tens de introduzir um dos operador permitidos!")
            print("Introduz o operador desejado (+, -, /, *, &&, ||, !, shl, shr): ")
            operador = readln()

            if (operador == "+" || operador == "-" || operador == "/" || operador == "*" ||
                operador == "||" || operador == "&&" || operador == "!" ||
                operador == "shl" || operador == "shr"
            )
                break
        }
    }

    // se o operador for o not não precisamos de um segundo valor e acaba aqui
    if (operador == "!") {
        val x = not(primeiroValor!!)
        val output = "!$primeiroValor: $x"
        print(output)
        return
    }

    // mesma lógica de continuar a pedir um número válido
    print("Introduz o segundo valor desejado: ")
    var segundoValor = readln().toFloatOrNull()

    if (segundoValor == null) {
        while (true) {
            print("Tens de introduzir um número: ")
            segundoValor = readln().toFloatOrNull()

            if (segundoValor != null)
                break
        }
    }

    var x = 0f
    var output = ""

    when (operador) {
        "+" -> {
            x = sum(primeiroValor!!, segundoValor!!)
            output = "$primeiroValor $operador $segundoValor: $x em decimal, " +
                    "${x.toInt().toString(16).uppercase()} em hexadecimal, " +
                    "${x != 0f} em booleano"
        }

        "-" -> {
            x = subtract(primeiroValor!!, segundoValor!!)
            output = "$primeiroValor $operador $segundoValor: $x em decimal, " +
                    "${x.toInt().toString(16).uppercase()} em hexadecimal, " +
                    "${x != 0f} em booleano"
        }

        "*" -> {
            x = multiply(primeiroValor!!, segundoValor!!)
            output = "$primeiroValor $operador $segundoValor: $x em decimal, " +
                    "${x.toInt().toString(16).uppercase()} em hexadecimal, " +
                    "${x != 0f} em booleano"
        }

        "/" -> {
            try {
                if (divideCheck(segundoValor!!)) {
                    x = divide(primeiroValor!!, segundoValor!!)
                    output = "$primeiroValor $operador $segundoValor: $x em decimal, " +
                            "${x.toInt().toString(16).uppercase()} em hexadecimal, " +
                            "${x != 0f} em booleano"
                }
            } catch (e: IllegalArgumentException) {
                println(e.message)
            }
        }

        "||" -> {
            val x = or(primeiroValor!!, segundoValor!!)
            output = "$primeiroValor $operador $segundoValor: $x"
        }

        "&&" -> {
            val x = and(primeiroValor!!, segundoValor!!)
            output = "$primeiroValor $operador $segundoValor: $x"
        }

        "shl" -> {
            x = shiftLeft(primeiroValor!!, segundoValor!!).toFloat()
            output = "$primeiroValor shl $segundoValor: $x em decimal, " +
                    "${x.toInt().toString(16).uppercase()} em hexadecimal, " +
                    "${Integer.toBinaryString(x.toInt())} em binário, " +
                    "${x != 0f} em booleano"
        }

        "shr" -> {
            x = shiftRight(primeiroValor!!, segundoValor!!).toFloat()
            output = "$primeiroValor shr $segundoValor: $x em decimal, " +
                    "${x.toInt().toString(16).uppercase()} em hexadecimal, " +
                    "${Integer.toBinaryString(x.toInt())} em binário, " +
                    "${x != 0f} em booleano"
        }
    }

    println(output)
}
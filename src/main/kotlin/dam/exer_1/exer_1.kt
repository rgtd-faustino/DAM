package org.example.dam.exer_1

fun main() {
    // using IntArray constructor
    // usamos i quando criamos uma variável lambda (antes da seta)
    // usamos it quando não criamos uma variável lambda (antes da seta)
    val array1 = IntArray(50) { i ->
        // kotlin diz para usarmos sempre val por defeito, como estamos a recriar a variável a cada iteração
        // e não a alterá-la podemos usar val
        // temos de fazer o indice mais 1 porque isto vai de 0 a 49
        val base = i + 1
        base * base
    }

    for (value in array1) {
        println(value)
    }

    val outputPlaceholder = "Array tem estes números: PLACEHOLDER"
    var output = outputPlaceholder.replace("PLACEHOLDER", array1.toList().toString()) // replace usado para trocar strings
    println(output)

    // using a range and map()
    // usamos i quando criamos uma variável lambda (antes da seta)
    // usamos it quando não criamos uma variável lambda (antes da seta)
    // o map itera sobre a range (1 a 50) e dentro no lambda multiplicamos o indice por começa logo desde o 1
    val lista = (1..50).map {
        it * it
    }

    val array2 = lista.toIntArray() // o map cria uma lista

    for (value in array2) {
        println(value)
    }

    output = outputPlaceholder.replace("PLACEHOLDER", array2.toList().toString()) // replace usado para trocar strings
    println(output)

    // using Array with constructor
    // usamos i quando criamos uma variável lambda (antes da seta)
    // usamos it quando não criamos uma variável lambda (antes da seta)
    val array3 = Array(50) { i ->
        // kotlin diz para usarmos sempre val por defeito, como estamos a recriar a variável a cada iteração
        // e não a alterá-la podemos usar val
        val base = i + 1
        base * base
    }

    for (value in array3) {
        println(value)
    }

    output = outputPlaceholder.replace("PLACEHOLDER", array3.toList().toString()) // replace usado para trocar strings
    println(output)
}
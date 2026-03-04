package org.example.dam.exer_3

fun main() {
    val currentHeight = 100f
    val newHeightPercent = 0.6f
    val minHeight = 1f
    val maxQualifyingBounces = 15

    // previous height vai mudando os valores à medida que a sequencia vai correndo, é o lambda
    val bounces = generateSequence(currentHeight) { previousHeight ->
        bounceBall(previousHeight, newHeightPercent, minHeight)
    }

    // o take apanha os primeiros n elementos ou seja primeiros 15
    val firstBounces = bounces.take(maxQualifyingBounces).toList()
    println(firstBounces)
}

// precisamos do ponto de interrogação porque o valor retornado pode ser null, não é sempre float
fun bounceBall(previousHeight: Float, newHeightPercent: Float, minHeight: Float): Float? {
    val newHeight = previousHeight * newHeightPercent

    if (newHeight >= minHeight)
        return newHeight
    else
        return null
}
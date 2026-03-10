package org.example.dam.exer_3

fun main() {
    val currentHeight = 100f
    val newHeightPercent = 0.6f
    val minHeight = 1f
    val maxQualifyingBounces = 15

    // previous height vai mudando os valores à medida que a sequencia vai correndo, é o lambda
    val bounces = generateSequence(currentHeight) {
        // o take if pára a sequência quando o novo valor it for menor que a minHeight
        // é preciso usar o it em vez do previous height porque queremos verificar se o novo valor é maior que minHeight
        previousHeight -> (previousHeight * newHeightPercent).takeIf { it >= minHeight }
    }

    // o take apanha os primeiros n elementos ou seja primeiros 15
    val firstBounces = bounces.take(maxQualifyingBounces)

    for (bounce in firstBounces) {
        println("%.2f".format(bounce)) // para ficar apenas duas casas decimais
    }
}

package dam.exer_4

import kotlin.math.sqrt

data class Vec2(val x: Double, val y: Double) : Comparable<Vec2> {
    // para dar override aos operadores temos de fazer desta maneira
    // basicamente pegamos no valor que estamos a somar e o outro valor que vem depois
    // (this é o vetor da esquerda, value é o vetor da direita)
    operator fun plus(value: Vec2): Vec2 {
        return Vec2(this.x + value.x, this.y + value.y)
    }

    operator fun minus(value: Vec2): Vec2 {
        return Vec2(this.x - value.x, this.y - value.y)
    }

    operator fun times(value: Double): Vec2 {
        return Vec2(this.x * value, this.y * value)
    }

    operator fun unaryMinus(): Vec2 {
        return Vec2(-this.x, -this.y)
    }

    // como a função compareTo originalmente só compara número, para comparar vetores temos de calcular as magnitudes
    // e depois como temos números já podemos usar a função compareTo original
    // damos override à função da interface Comparable e metemos o nome do param igual à função original
    override operator fun compareTo(other: Vec2): Int {
        val thisMagnitude = sqrt(this.x * this.x + this.y * this.y) // apanhamos a magnitude do primeiro valor
        val otherMagnitude = sqrt(other.x * other.x + other.y * other.y) // apanhamos a magnitude do segundo valor
        return thisMagnitude.compareTo(otherMagnitude)
    }

    // fórmula euclidiana
    fun magnitude(): Double {
        return sqrt(x * x + y * y)
    }

    // fórmula dot (produto interno)
    fun dot(value: Vec2): Double {
        return this.x * value.x + this.y * value.y
    }

    // a normalização a fórmula é simplesmente dividir cada componente pela magnitude
    // o magnitude() apanha a magnitude do valor onde estamos a chamar esta função e depois se for 0 é porque
    // o vetor é (0, 0) e não podemos dividir por 0, senão damos return
    fun normalized(): Vec2 {
        val mag = magnitude()

        if (mag == 0.0) {
            throw IllegalStateException("Não podes normalizar um vetor (0, 0) porque não podemos dividir por 0")
        }

        return Vec2(x / mag, y / mag)
    }

    operator fun get(index: Int): Double {
        if (index == 0) {
            return x

        } else if (index == 1) {
            return y

        } else {
            throw IndexOutOfBoundsException("O index tem que ser 0 ou 1")
        }
    }
}
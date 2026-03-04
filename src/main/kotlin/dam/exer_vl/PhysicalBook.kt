package org.example.dam.exer_vl

// o super são os parâmetros dentro da classe que estamos a estender
class PhysicalBook(title: String, author: String, publicationYear: Int, availableCopies: Int, weight: Int,
                   hasHardcover: Boolean = true) : Book(title, author, publicationYear, availableCopies) {
    val weight: Int
    val hasHardcover: Boolean

    init {
        this.weight = weight
        this.hasHardcover = hasHardcover
    }

    override fun toString(): String {
        return "Title: ${this.title}, Author: ${this.author}, Era: ${this.era}, Available copies: ${this.availableCopies}"
    }

    override fun getStorageInfo():String {
        return "Storage: Physical book: ${this.weight}g, Hardcover: ${if (hasHardcover) "Yes" else "No"}."
    }
}
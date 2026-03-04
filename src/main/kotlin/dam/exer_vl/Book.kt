package org.example.dam.exer_vl

abstract class Book(title:String, author:String, publicationYear: Int, availableCopiesGetter:Int) {
    val title: String
    val author: String
    val publicationYear: Int

    // getter para a era de acordo com o ano de publicação
    val era: String
        get() = when {
            publicationYear < 1980 -> "Classic"
            publicationYear <= 2010 -> "Modern"
            else -> "Contemporary"
        }

    // setter para as cópias disponíveis, availableCopies fica privado e usamos availableCopiesGetter para o publico
    var availableCopies: Int = availableCopiesGetter
        set(value) {
            // atribui o valor quando é positivo senão diz que não pode ser atribuído um valor negativo
            // meti availableCopies = value mas ficava com traço amarelo e a solução era usar field
            if(value >= 0)
                field = value
        }

    init {
        this.title = title
        this.author = author
        this.publicationYear = publicationYear
        println("The book named ${this.title} written by ${this.author} has been added to the library!")
    }



    // é mais fácil criar esta função que dá decrease automaticamente por um valor
    fun decreaseAvailableCopies(){
        if(this.availableCopies > 0){
            this.availableCopies -= 1
            println("Successfully borrowed ${this.title}. Copies remaining: ${this.availableCopies}")

            if(this.availableCopies == 0)
                println("Warning: Book is now out of stock!")

        } else
            println("Book ${this.title} couldn't be borrowed: there are no available copies!")

    }

    // e então podemos fazer a mesma coisa para o adicionar por um valor
    fun increaseAvailableCopies(){
        this.availableCopies += 1
    }

    // tecnicamente não é preciso porque a classe é abstrata e não existe um objeto Book, mas se uma subclasse não tiver
    // um método to string override então usa este
    override fun toString(): String {
        return "Title: ${this.title}, Author: ${this.author}, Era: ${this.era}, Available copies: ${this.availableCopies}"
    }

    abstract fun getStorageInfo():String
}
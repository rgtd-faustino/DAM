package org.example.dam.exer_vl

// o super são os parâmetros dentro da classe que estamos a estender
class DigitalBook(title: String, author: String, publicationYear: Int, availableCopies: Int, fileSize: Float,
                  format: String) : Book(title, author, publicationYear, availableCopies) {
    val fileSize: Float
    val format: String

    init {
        this.fileSize = fileSize
        this.format = format
    }

    override fun getStorageInfo(): String {
        return "Storage: Stored digitally: ${this.fileSize} MB, Format: ${this.format}."
    }
}
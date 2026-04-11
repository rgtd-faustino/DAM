package org.example.dam.exer_vl

class Library(name:String) {
    companion object {
        var totalBooks = 0

        fun getTotalBooksCreated() {
            println("Number of books created across all libraries: ${totalBooks}")
        }
    }

    var booksList: MutableList<Book> = mutableListOf()

    fun addBook(book: Book){
        booksList.add(book)
        totalBooks++
    }

    fun borrowBook(title: String){
        var book:Book? = null // sem o ? não posso dizer que o livro é nulo e sem o nulo dá erro no if

        for (bookInList in booksList) {
            if (bookInList.title == title) {
                book = bookInList
                break
            }
        }

        if (book != null)
            book.decreaseAvailableCopies()
        else
            println("Error: Book ${title} not found in library.")
    }

    fun returnBook(title:String){
        var book:Book? = null // sem o ? não posso dizer que o livro é nulo e sem o nulo dá erro no if

        for (bookInList in booksList) {
            if (bookInList.title == title) {
                book = bookInList
                break
            }
        }

        if (book != null) {
            book.increaseAvailableCopies()
            println("Book ${book.title} successfully returned. Copies available: ${book.availableCopies}")
        } else
            println("Book not found.")

    }

    fun showBooks(){
        println("==== Books ====")
        for(book in booksList){
            println(book)
            println(book.getStorageInfo())
        }
    }

    fun searchByAuthor(author: String) {
        var foundBook = false
        println("Books by $author:")

        for (book in booksList) {
            if (book.author == author) {
                foundBook = true
                println("- ${book.title} (${book.era}, ${book.availableCopies} " +
                        "${if (book.availableCopies != 1) "copies" else "copy"} available)")
            }
        }

        if (foundBook == false) {
            println("No books were found.")
        }
    }
}
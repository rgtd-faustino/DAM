package org.example.dam.exer_1

import org.example.dam.exer_1.Event.*


sealed class Event {

    // criei variáveis dentro de cada classe para podermos aceder aos parâmetros quando precisarmos nas funções
    class Login(val u: String, val ts: Long) : Event() {
        val username = u
        val timestamp = ts
    }

    class Purchase(val u: String, val a: Double, val ts: Long) : Event(){
        val username = u
        val amount = a
        val timestamp = ts
    }


    class Logout(val u: String, val ts: Long) : Event(){
        val username = u
        val timestamp = ts

    }
}

// como isto é uma função por extensão o this representa a lista onde a função está a ser chamada
// ou seja temos acesso aos eventos na lista, então é só percorrermos a mesma e compararmos os nomes
// no fim adicionamos os eventos à lista e damos print
fun List<Event>.filterByUser(parameter: String): List<Event> {
    val list = mutableListOf<Event>()

    // - 1 senão dá erro de index
    for(i in 0 .. this.size - 1) {
        if ((this.get(i) is Login) && (this.get(i) as Login).username == parameter)
            list.add(this.get(i))
        if ((this.get(i) is Purchase) && (this.get(i) as Purchase).username == parameter)
            list.add(this.get(i))
        if ((this.get(i) is Logout) && (this.get(i) as Logout).username == parameter)
            list.add(this.get(i))
    }

    println("Events for $parameter:")
    for (event in list) {
        when (event) {
            // comparamos o tipo de evento para sabermos que output damos
            is Login -> println("Login (username = ${event.username}, timestamp = ${event.timestamp})")
            is Purchase -> println("Purchase (username = ${event.username}, amount = ${event.amount}, timestamp = ${event.timestamp})")
            is Logout -> println("Logout (username = ${event.username}, timestamp = ${event.timestamp})")
        }
    }

    return list
}

// percorremos a lista toda e somamos valores se o evento for Purchase
// não metemos aqui duas casas decimais no output porque assim o mesmo teria de ser uma string em vez de double
fun List<Event>.totalSpent(parameter: String): Double{
    var total = 0.0

    for(i in 0 .. this.size - 1) {
        if ((this.get(i) is Purchase) && (this.get(i) as Purchase).username == parameter)
            total += (this.get(i) as Purchase).amount
    }

    return total
}

// metemos o evento no lambda (handler) para depois ser usado como quisermos
// como a função não tem return type metemos unit que é void em java
fun List<Event>.processEvents(handler: (Event) -> Unit) {
    for (event in this) {
        handler(event)
    }
}
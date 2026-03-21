package org.example.dam.exer_1

import org.example.dam.exer_1.Event.*


sealed class Event {

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


fun List<Event>.filterByUser(parameter: String): List<Event> {
    val list = mutableListOf<Event>()

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
            is Login -> println("Login (username = ${event.username}, timestamp = ${event.timestamp})")
            is Purchase -> println("Purchase (username = ${event.username}, amount = ${event.amount}, timestamp = ${event.timestamp})")
            is Logout -> println("Logout (username = ${event.username}, timestamp = ${event.timestamp})")
        }
    }

    return list
}

fun List<Event>.totalSpent(parameter: String): Double{
    var total = 0.0

    for(i in 0 .. this.size - 1) {
        if ((this.get(i) is Purchase) && (this.get(i) as Purchase).username == parameter)
            total += (this.get(i) as Purchase).amount
    }

    return total
}


fun List<Event>.processEvents(handler: (Event) -> Unit) {
    for (event in this) {
        handler(event)
    }
}
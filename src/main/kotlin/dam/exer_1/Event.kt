package org.example.dam.exer_1

sealed class Event {
    class Login(val username: String, val timestamp: Long){

    }

    class Purchase(val username: String, val amount: Double, val timestamp: Long){

    }


    class Logout(val username: String, val timestamp: Long){

    }

    fun List<Event>.filterByUser(parameter: String){
        return
    }

    fun List<Event>.totalSpent(parameter: String){
        return
    }

    fun List<Event>.processEvents(handler: (Event) -> Unit): Unit {

    }

}
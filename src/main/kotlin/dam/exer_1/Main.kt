package org.example.dam.exer_1

fun main() {
    val events = listOf(
        Event.Login("alice", 1_000),
        Event.Purchase("alice", 49.99, 1_100),
        Event.Purchase("bob", 19.99, 1_200),
        Event.Login("bob", 1_050),
        Event.Purchase("alice", 15.00, 1_300),
        Event.Logout("alice", 1_400),
        Event.Logout("bob", 1_500)
    )

    // usamos o lambda que está na função para fazermos o que quisermos, neste caso dar output das informações de cada um
    events.processEvents { event ->
        when (event) {
            // comparamos o tipo de evento para sabermos que output damos
            is Event.Login -> println("[LOGIN] ${event.username} logged in at t = ${event.timestamp}")
            is Event.Purchase -> println("[PURCHASE] ${event.username} spent $${event.amount} at t = ${event.timestamp}")
            is Event.Logout -> println("[LOGOUT] ${event.username} logged out at t = ${event.timestamp}")
        }
    }

    println()

    // para o formato original continuar double aqui é que passamos a string e damos output com duas casas decimais
    println("Total spent by alice: ${"%.2f".format(events.totalSpent("alice"))}")
    println("Total spent by bob: ${"%.2f".format(events.totalSpent("bob"))}")

    println()

    events.filterByUser("alice")
}
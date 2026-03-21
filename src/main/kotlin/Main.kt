package org.example

import org.example.dam.exer_1.Event
import org.example.dam.exer_1.filterByUser
import org.example.dam.exer_1.processEvents
import org.example.dam.exer_1.totalSpent

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

    events.processEvents { event ->
        when (event) {
            is Event.Login    -> println("[LOGIN] ${event.username} logged in at t = ${event.timestamp}")
            is Event.Purchase -> println("[PURCHASE] ${event.username} spent $${event.amount} at t = ${event.timestamp}")
            is Event.Logout   -> println("[LOGOUT] ${event.username} logged out at t = ${event.timestamp}")
        }
    }

    println()

    println("Total spent by alice: ${"%.2f".format(events.totalSpent("alice"))}")
    println("Total spent by bob: ${"%.2f".format(events.totalSpent("bob"))}")

    println()

    events.filterByUser("alice")
}
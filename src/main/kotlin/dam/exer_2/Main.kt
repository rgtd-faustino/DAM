package dam.exer_2

import org.example.dam.exer_2.Cache

fun main() {
    val wordCache = Cache<String, Int>()
    wordCache.put("kotlin", 1)
    wordCache.put("scala", 1)
    wordCache.put("haskell", 1)

    println("--- Word frequency cache ---")
    println("Size: ${wordCache.size()}")
    println("Frequency of 'kotlin': ${wordCache.get("kotlin")}")
    println("getOrPut 'kotlin': ${wordCache.getOrPut("kotlin") { 0 }}")
    println("getOrPut 'java': ${wordCache.getOrPut("java") { 0 }}")
    println("Size after getOrPut: ${wordCache.size()}")
    println("Transform 'kotlin' (+1): ${wordCache.transform("kotlin") { it + 1 }}")
    println("Transform 'cobol' (+1): ${wordCache.transform("cobol") { it + 1 }}")
    println("Snapshot : ${wordCache.snapshot()}")

    val idCache = Cache<Int, String>()
    idCache.put(1, "Alice")
    idCache.put(2, "Bob")

    println()

    println("--- Id registry cache ---")
    println("Id 1 -> ${idCache.get(1)}")
    println("Id 2 -> ${idCache.get(2)}")
    idCache.evict(1)
    println("After evict id 1, size : ${idCache.size()}")
    println("Id 1 after evict -> ${idCache.get(1)}")

    println()

    // relembrar que o it apanha os elementos que estão dentro da lista em si
    // então não é preciso fazer um for loop
    println("Words with count > 0 : ${wordCache.filterValues { it > 0 }}")
}
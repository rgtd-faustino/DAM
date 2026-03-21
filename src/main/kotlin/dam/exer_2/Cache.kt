package org.example.dam.exer_2

class Cache<K : Any, V : Any> {
    var mapa = mutableMapOf<K, V>()

    fun put(key: K, value: V){

    }

    fun get(key: K): V?{

    }

    fun evict(key: K){

    }

    fun size(): Int{

    }

    fun getOrPut(key: K, default: () -> V): V{

    }

    fun transform(key: K, action: (V) -> V): Boolean{

    }

    fun snapshot(): Map<K, V>{

    }

}
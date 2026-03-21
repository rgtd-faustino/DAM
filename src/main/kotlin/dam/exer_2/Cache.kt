package org.example.dam.exer_2

class Cache<K : Any, V : Any> {
    var mapa = mutableMapOf<K, V>()

    fun put(key: K, value: V){
        mapa[key] = value
    }

    fun get(key: K): V?{
        return mapa[key]
    }

    fun evict(key: K){
        mapa.remove(key)
    }

    fun size(): Int{
        return mapa.size
    }

    fun getOrPut(key: K, default: () -> V): V{
        // se o valor estiver presente então damos return, senão colocamo-lo no mapa
        return mapa.getOrPut(key, default)
    }


    fun transform(key: K, action: (V) -> V): Boolean {
        // primeiro temos de verificar que a chave existe no mapa
        if (mapa.contains(key)) {
            val value = mapa[key]
            // como o action precisa de uma garantia que o valor existe metemos os !! que obriga que não seja nulo
            mapa[key] = action(value!!)
            return true
        }
        return false
    }

    fun snapshot(): Map<K, V>{
        // isto retorna uma cópia read only ou seja imutável do mapa original com a mesma ordem e conteúdo
        return mapa.toMap(mutableMapOf())
    }

}
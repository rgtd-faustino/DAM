package com.example.vaultguard.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path


// a API escolhida para o trabalho: manda os priemiros 5 caracteres do hash em vez da password
// depois é nos retornado uma lista com as hashes que começam assim e nós comparamos com o resto
// dos caracteres da nossa password
interface HaveIBeenPwnedApi {
    // faz um pedido HTTP GET para o url range/XXXXX
    // onde XXXXX são os primeiros 5 caracteres do hash SHA-1
    @GET("range/{hashPrefix}")
    // hashPrefix depois é substituido pelo valor do parâmetro
    suspend fun getPasswordBreaches(@Path("hashPrefix") hashPrefix: String): ResponseBody
}

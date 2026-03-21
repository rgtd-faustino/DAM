package com.example.vaultguard.data.repository

import com.example.vaultguard.data.crypto.CryptoManager
import com.example.vaultguard.data.local.dao.PasswordDao
import com.example.vaultguard.data.local.entity.PasswordEntry
import com.example.vaultguard.data.remote.api.HaveIBeenPwnedApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    private val passwordDao: PasswordDao, // ler escrever na base de dados
    private val cryptoManager: CryptoManager, // cifrar/decifrar dados antes de
    private val pwnedApi: HaveIBeenPwnedApi // para ve se a pass já foi comprometida (API)
) {
    fun getAllEntries(): Flow<List<PasswordEntry>> {
        return passwordDao.getAllEntries()
    }

    // este ID é um número inteiro criado automaticamente pelo Room quando uma entrada é inserida
    // é a chave primária da tabela
    suspend fun getEntryById(id: Int): PasswordEntry? {
        return passwordDao.getEntryById(id)
    }

    // tudo o que o utilizador escreveu fica guardado como encriptado
    suspend fun addEntry(entry: PasswordEntry) {
        val encryptedEntry = entry.copy(
            username = cryptoManager.encrypt(entry.username),
            password = cryptoManager.encrypt(entry.password),
            notes = cryptoManager.encrypt(entry.notes)
        )
        passwordDao.insertEntry(encryptedEntry)
    }

    suspend fun updateEntry(entry: PasswordEntry) {
        val encryptedEntry = entry.copy(
            username = cryptoManager.encrypt(entry.username),
            password = cryptoManager.encrypt(entry.password),
            notes = cryptoManager.encrypt(entry.notes)
        )
        passwordDao.updateEntry(encryptedEntry)
    }

    suspend fun deleteEntry(entry: PasswordEntry) {
        passwordDao.deleteEntry(entry)
    }

    fun decrypt(encryptedBase64: String): String {
        return cryptoManager.decrypt(encryptedBase64)
    }
    
    fun encrypt(plainText: String): String {
        return cryptoManager.encrypt(plainText)
    }


    // se o user clicar no botão de checkar se a pass foi comprometida esta função é chamada
    // como explicado anteriormente, o objetivo é mandar os primeiros 5 caracteres do hash da pass
    // recebemos uma lista de hashes que começam assim e tentamos encontrar a nossa hash
    // o with Context é obrigatório porque o android proíbe chamadas de rede na thread principal
    // (iria travar a interface)
    suspend fun checkPasswordExposed(plainPassword: String): Int = withContext(Dispatchers.IO) {
        try {
            val md = MessageDigest.getInstance("SHA-1")
            val digest = md.digest(plainPassword.toByteArray(Charsets.UTF_8))
            val sha1Hex = digest.joinToString("") { "%02x".format(it) }.uppercase()
            
            val prefix = sha1Hex.take(5)
            val suffix = sha1Hex.drop(5)
            
            val responseBody = pwnedApi.getPasswordBreaches(prefix)
            val responseString = responseBody.string()
            
            val lines = responseString.lines()
            for (line in lines) {
                if (line.startsWith(suffix)) {
                    val countStr = line.substringAfter(":").trim()
                    return@withContext countStr.toIntOrNull() ?: 0
                }
            }
            return@withContext 0
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext -1 // Error occurred
        }
    }
}

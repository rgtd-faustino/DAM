package com.example.vaultguard.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    // o contexto da aplicação é necessário para aceder a ficheiros, preferências, etc.
    // neste caso é o acesso ao sistema do android
    @ApplicationContext private val context: Context
) {

    // a master key aqui serve para proteger o ficheiro de preferências onde o PIN hash está guardado
    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // shared preferences normalmente guarda em texto simples, mas tem encrypted e funciona tanto
    // para as chaves como os valores
    // guarda dentro do armazenamento privado da app no ficheiro "secure_vault_prefs"
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_vault_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // quando o user está na página de login
    // isto decide se mostramos para criar um pin ou dar login com o PIN com o setupPIN
    fun hasPinSetup(): Boolean {
        return sharedPreferences.contains(KEY_PIN_HASH)
    }

    // caso não tenhamos ainda criado um PIN então editamos os sharedPreferences para colocar lá
    // o hash do pin
    fun setupPin(pin: String) {
        val hash = hashPin(pin)
        sharedPreferences.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    // apanhamos o hash caso exista e se for igual ao hash do pin que introduzirmos no login então
    // é porque podemos dar login
    fun verifyPin(pin: String): Boolean {
        val storedHash = sharedPreferences.getString(KEY_PIN_HASH, null) ?: return false
        val inputHash = hashPin(pin)
        return storedHash == inputHash
    }

    // para criar o hash usamos o algoritmo SHA-256 porque é muito forte e são 32 bytes sempre
    // independentemente do input,
    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
        // "%02x" -> converte cada byte para dois caracteres hexadecimais o que faz com que o
        // resultado seja sempre uma string de 64 caracteres
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
    }
}

package com.example.vaultguard.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton


// singleton faz com que só exista uma instância desta classe na app inteira, é normalmente usado
// para managers então faz sentido
// inject é injeção de dependências, basicamente é para deixar o sistema criar esta classe automaticamente
// quando alguém precisar dela sem ter de escrever CryptoManager() manualmente
@Singleton
class CryptoManager @Inject constructor() {

    // key store é o cofre seguro do android onde são guardadas chaves criptográficas a nível do
    // sistema operativo e o get instance é para obter o cofre
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        // isto não abre o keystore é só para o inicializar na memória
        load(null)
    }

    // vai ao cofre e tenta apanhar a chave com o nome ALIAS ("VaultGuard_CryptoKey")
    // depois tenta converter para secretKeyEntry senão fica null
    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        // ?: -> se o lado esquerdo for null executa o lado direito
        return existingKey?.secretKey ?: createSecretKey()
    }

    // pede ao android um gerador de chaves AES (algoritmo de cifra)
    private fun createSecretKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore").apply {
            init(
                // para dizer que a chave serve para encriptar ou desencriptar
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                // a chave não requer impressão digital para ser usada
                .setUserAuthenticationRequired(false)
                // para que textos iguais tenham cifras diferentes
                .setRandomizedEncryptionRequired(true)
                .build()
            )
        }.generateKey() // guarda a chave no cofre
    }

    // recebe um texto e devolve o texto cifrado
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        // Cipher = máquina de cifrar e dizemos que combinação de algoritmo, modo de operação e que padding usar
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // modo de encriptação nesta função
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv // para que o mesmo texto dê resultados diferentes
        // temos de passar o texto para bytes porque o computador só trabalha com bytes
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // juntar tudo para depois conseguir decifrar
        // 1 byte -> tamanho do IV
        // iv.size -> o IV em si
        // encryptedBytes.size -> a cifra
        val combined = ByteArray(1 + iv.size + encryptedBytes.size)
        combined[0] = iv.size.toByte() //pq o array é de bytes
        // usamos array copy porque se formos diretamente com o combined[index] pode haver erros
        // caso o IV mude de tamanho porque teriamos de navegar o array por combined[1 + iv.size]
        System.arraycopy(iv, 0, combined, 1, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, 1 + iv.size, encryptedBytes.size)

        // converte os bytes para texto legível sem quebras de linhas (no_wrap)
        // usamos base64 porque não podemos guardar bytes diretamente como strings senão os dados
        // ficam corrompidos
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    // faz o processo inverso que a função encrypt
    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        
        val ivSize = combined[0].toInt()
        val iv = ByteArray(ivSize)
        System.arraycopy(combined, 1, iv, 0, ivSize)
        
        val encryptedBytesSize = combined.size - 1 - ivSize
        val encryptedBytes = ByteArray(encryptedBytesSize)
        System.arraycopy(combined, 1 + ivSize, encryptedBytes, 0, encryptedBytesSize)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        // agora usamos o decrypt
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }


    // a google recomenda AES/GCM/NoPadding para android
    companion object {

        // AES faz blocos de 16 bytes, muito usado
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

        // entre ECB CBC CTR e GCM este era o melhor
        // um block mode define como os blocos são ligados entre si
        // digamos que temos uma mensagem grande, é dividida em 3 blocos
        // o block mode define como são ligados, se são tratados isoladamente ou dependendo dos anteriores
        // o GCM faz um keystream entre os blocos com XOR e autenticação ao mesmo tempo
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM

        //GCM não precisa de padding
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING" // AES GCM NoPadding

        // o alias serve como um endereço para sabermos qual é a chave, todas as passwords usam este alias
        // o que muda depois é o IV, a chave é igual
        private const val ALIAS = "VaultGuard_CryptoKey"
    }
}

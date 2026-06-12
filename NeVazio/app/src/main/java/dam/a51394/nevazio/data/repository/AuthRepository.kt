package dam.a51394.nevazio.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    
    val currentUser get() = auth.currentUser
    
    suspend fun login(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(email: String, pass: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        auth.signOut()
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Esta lógica implementa a arquitetura de Anfitrião/Convidado (Host/Guest). 
    // Em vez do código de família ser diretamente o ID do frigorífico (o que apagaria os dados
    // originais do criador), o código de família funciona como uma "chave de mapeamento".
    // A consulta à coleção 'family_codes' devolve o UID do utilizador que atua como anfitrião.
    // Esta abordagem garante que o anfitrião não perde dados ao criar uma família e que os
    // dados dos convidados ficam em segurança e ocultos até abandonarem a família.
    suspend fun getCurrentFridgeId(): String {
        val user = currentUser ?: return "default_fridge"
        return try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val doc = db.collection("users").document(user.uid).get().await()
            val fCode = doc.getString("familyCode")
            
            if (!fCode.isNullOrBlank()) {
                val cleanCode = fCode.trim()
                // A validação do mapeamento procura a quem pertence este código de família.
                val familyDoc = db.collection("family_codes").document(cleanCode).get().await()
                val hostUid = familyDoc.getString("hostUid")
                
                // Se o mapeamento existir e for válido, a aplicação direciona a leitura para
                // o frigorífico do anfitrião. Caso contrário, ocorre um fallback de segurança
                // para o frigorífico pessoal do próprio utilizador.
                if (!hostUid.isNullOrBlank()) {
                    hostUid
                } else {
                    user.uid
                }
            } else {
                user.uid
            }
        } catch (e: Exception) {
            user.uid
        }
    }
}

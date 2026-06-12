package dam.a51394.nevazio.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dam.a51394.nevazio.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = authRepository.currentUser
        if (user == null) {
            _uiState.update { it.copy(isLoggedOut = true) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val document = db.collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: user.email ?: ""
                    val familyCode = document.getString("familyCode") ?: ""
                    
                    _uiState.update {
                        it.copy(
                            name = name,
                            email = email,
                            familyCode = familyCode,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            email = user.email ?: "",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao carregar perfil",
                        email = user.email ?: ""
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }

    fun toggleChangeNameDialog(show: Boolean) {
        _uiState.update { it.copy(showChangeNameDialog = show, newNameInput = if (show) it.name else "", errorMessage = null, successMessage = null) }
    }

    fun toggleChangePasswordDialog(show: Boolean) {
        _uiState.update { it.copy(showChangePasswordDialog = show, newPasswordInput = "", errorMessage = null, successMessage = null) }
    }

    fun toggleJoinFamilyDialog(show: Boolean) {
        _uiState.update { it.copy(showJoinFamilyDialog = show, newFamilyCodeInput = "", errorMessage = null, successMessage = null) }
    }

    fun toggleLeaveFamilyDialog(show: Boolean) {
        _uiState.update { it.copy(showLeaveFamilyDialog = show, errorMessage = null, successMessage = null) }
    }

    fun onNewNameChange(name: String) {
        _uiState.update { it.copy(newNameInput = name) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPasswordInput = password) }
    }

    fun onNewFamilyCodeChange(code: String) {
        _uiState.update { it.copy(newFamilyCodeInput = code) }
    }

    fun updateName() {
        val user = authRepository.currentUser ?: return
        val newName = _uiState.value.newNameInput

        if (newName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "O nome não pode estar vazio") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                db.collection("users").document(user.uid)
                  .set(hashMapOf("name" to newName), SetOptions.merge())
                  .await()
                  
                _uiState.update { 
                    it.copy(
                        name = newName,
                        isLoading = false,
                        showChangeNameDialog = false,
                        successMessage = "Nome atualizado com sucesso!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        showChangeNameDialog = false,
                        errorMessage = "Erro ao atualizar nome: ${e.message}" 
                    ) 
                }
            }
        }
    }

    fun updatePassword() {
        val user = authRepository.currentUser ?: return
        val newPassword = _uiState.value.newPasswordInput

        if (newPassword.length < 6) {
            _uiState.update { 
                it.copy(
                    errorMessage = "A password deve ter pelo menos 6 caracteres",
                    showChangePasswordDialog = false
                ) 
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                user.updatePassword(newPassword).await()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        showChangePasswordDialog = false,
                        successMessage = "Password atualizada com sucesso!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        showChangePasswordDialog = false,
                        errorMessage = "Erro ao atualizar password. A sessão pode ter expirado." 
                    ) 
                }
            }
        }
    }
    
    fun joinFamily() {
        val user = authRepository.currentUser ?: return
        
        // A função trim() é aplicada ao código inserido para mitigar problemas com copy-paste,
        // que frequentemente inclui espaços em branco invisíveis no início ou no fim. 
        // Esta normalização evita que o espaço seja interpretado como parte da string,
        // o que prenderia a conta num "frigorífico fantasma" desconectado do frigorífico familiar correto.
        val code = _uiState.value.newFamilyCodeInput.trim().uppercase()
        if (code.isBlank()) return
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Tentativa primária: verificar a coleção 'family_codes' (sistema atual).
                var familyDoc = db.collection("family_codes").document(code).get().await()
                
                if (!familyDoc.exists()) {
                    // Fallback de retrocompatibilidade: famílias criadas antes da introdução da
                    // coleção 'family_codes' apenas têm o código no campo 'familyCode' do perfil
                    // do anfitrião, sem documento de mapeamento. Esta pesquisa na coleção 'users'
                    // recupera o anfitrião e cria o documento em falta automaticamente,
                    // evitando que contas antigas fiquem permanentemente incompatíveis.
                    val usersWithCode = db.collection("users")
                        .whereEqualTo("familyCode", code)
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!usersWithCode.isEmpty) {
                        val hostUid = usersWithCode.documents.first().id
                        // Cria o documento de mapeamento em falta para reparar a inconsistência.
                        db.collection("family_codes").document(code)
                            .set(hashMapOf("hostUid" to hostUid))
                            .await()
                    } else {
                        // Código genuinamente inválido — não existe em nenhum sistema.
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Código de família inválido ou inexistente."
                            )
                        }
                        return@launch
                    }
                }

                db.collection("users").document(user.uid)
                  .set(hashMapOf("familyCode" to code), SetOptions.merge())
                  .await()
                  
                _uiState.update { 
                    it.copy(
                        familyCode = code,
                        isLoading = false,
                        showJoinFamilyDialog = false,
                        successMessage = "Juntaste-te à família com sucesso!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        showJoinFamilyDialog = false,
                        errorMessage = "Erro ao juntar família: ${e.message}" 
                    ) 
                }
            }
        }
    }

    fun createFamily() {
        val user = authRepository.currentUser ?: return
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val newCode = (1..6).map { chars.random() }.joinToString("")
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // O registo do código na coleção 'family_codes' funciona como uma tabela de mapeamento.
                // A definição do 'hostUid' estabelece este utilizador como Anfitrião, garantindo que
                // os dados associados ao seu UID pessoal se tornam na base de dados partilhada.
                db.collection("family_codes").document(newCode)
                  .set(hashMapOf("hostUid" to user.uid))
                  .await()

                db.collection("users").document(user.uid)
                  .set(hashMapOf("familyCode" to newCode), SetOptions.merge())
                  .await()
                  
                _uiState.update { 
                    it.copy(
                        familyCode = newCode,
                        isLoading = false,
                        successMessage = "Família criada! Partilha o teu código."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Erro ao criar família: ${e.message}" 
                    ) 
                }
            }
        }
    }

    fun leaveFamily() {
        val user = authRepository.currentUser ?: return
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                db.collection("users").document(user.uid)
                  .set(hashMapOf("familyCode" to ""), SetOptions.merge())
                  .await()
                  
                _uiState.update { 
                    it.copy(
                        familyCode = "",
                        isLoading = false,
                        showLeaveFamilyDialog = false,
                        successMessage = "Saíste da família."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        showLeaveFamilyDialog = false,
                        errorMessage = "Erro ao sair da família: ${e.message}" 
                    ) 
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

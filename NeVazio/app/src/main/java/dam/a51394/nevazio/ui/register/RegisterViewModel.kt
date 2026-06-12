package dam.a51394.nevazio.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam.a51394.nevazio.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun onFamilyCodeChange(value: String) = _uiState.update { it.copy(familyCode = value) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleConfirmPasswordVisibility() = _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    fun toggleTerms(accepted: Boolean) = _uiState.update { it.copy(termsAccepted = accepted) }

    fun onRegister(onSuccess: () -> Unit) {
        val state = _uiState.value
        when {
            state.name.isBlank() || state.email.isBlank() || state.password.isBlank() ->
                _uiState.update { it.copy(errorMessage = "Preenche todos os campos obrigatórios") }
            state.password != state.confirmPassword ->
                _uiState.update { it.copy(errorMessage = "As passwords não coincidem") }
            state.password.length < 8 ->
                _uiState.update { it.copy(errorMessage = "A password deve ter mínimo 8 caracteres") }
            !state.termsAccepted ->
                _uiState.update { it.copy(errorMessage = "Aceita os Termos de Serviço para continuar") }
            else -> {
                _uiState.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    val result = authRepository.register(state.email, state.password)
                    if (result.isSuccess) {
                        val user = authRepository.currentUser
                        if (user != null) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            
                            // A limpeza e verificação do código de família evita espaços acidentais.
                            val hasInputCode = state.familyCode.isNotBlank()
                            val cleanInputCode = state.familyCode.trim().uppercase()
                            
                            val finalFamilyCode = if (hasInputCode) {
                                cleanInputCode
                            } else {
                                java.util.UUID.randomUUID().toString().substring(0, 6).uppercase()
                            }
                            
                            val profileData = hashMapOf(
                                "name" to state.name,
                                "email" to user.email,
                                "uid" to user.uid,
                                "familyCode" to finalFamilyCode,
                                "createdAt" to com.google.firebase.Timestamp.now()
                            )
                            db.collection("users").document(user.uid).set(profileData)
                            
                            // A arquitetura baseia-se num mapeamento Anfitrião-Convidado.
                            // Se o utilizador não forneceu um código existente, assume-se que está a criar
                            // uma nova família raiz. Portanto, temos de criar explicitamente o documento de 
                            // mapeamento na coleção 'family_codes', onde este utilizador se assume como 'hostUid'.
                            // Isto previne a dependência excessiva no fallback automático do joinFamily.
                            if (!hasInputCode) {
                                db.collection("family_codes").document(finalFamilyCode)
                                    .set(hashMapOf("hostUid" to user.uid))
                            }
                        }
                        _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                        onSuccess()
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Erro no registo") }
                    }
                }
            }
        }
    }
}

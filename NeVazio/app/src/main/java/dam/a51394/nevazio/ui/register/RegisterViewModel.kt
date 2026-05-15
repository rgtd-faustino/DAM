package dam.a51394.nevazio.ui.register

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {
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
                _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                onSuccess()
            }
        }
    }
}

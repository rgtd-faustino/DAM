package dam.a51394.nevazio.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam.a51394.nevazio.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onLogin(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preenche todos os campos") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val result = authRepository.login(state.email, state.password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Erro no login") }
            }
        }
    }

    fun onResetPassword() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Insere o teu email para recuperar a password.") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null, message = null) }
        viewModelScope.launch {
            val result = authRepository.resetPassword(state.email)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, message = "Email de recuperação enviado!") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erro a recuperar password.") }
            }
        }
    }
}

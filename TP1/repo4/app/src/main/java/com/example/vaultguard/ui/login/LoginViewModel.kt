package com.example.vaultguard.ui.login

import androidx.lifecycle.ViewModel
import com.example.vaultguard.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkPinStatus()
    }

    // asim que é criado pergunta ao authRepo se já existe um pin criado
    // se sim então mostra para criar um, senão mostra para dar login
    private fun checkPinStatus() {
        _uiState.value = _uiState.value.copy(
            isSetupMode = !authRepository.hasPinSetup()
        )
    }

    fun submitPin(pin: String) {
        // obrigatório o pin ser maior do que 4 números
        if (pin.length < 4) {
            _uiState.value = _uiState.value.copy(error = "PIN must be at least 4 digits")
            return
        }

        // se tiver sido criado dá setup e estamos autenticados
        // se estivermos a dar login tem que ser válido senão dá erro
        if (_uiState.value.isSetupMode) {
            authRepository.setupPin(pin)
            _uiState.value = _uiState.value.copy(isAuthenticated = true, error = null)
        } else {
            val isValid = authRepository.verifyPin(pin)
            if (isValid) {
                _uiState.value = _uiState.value.copy(isAuthenticated = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(error = "Invalid PIN")
            }
        }
    }
}

data class LoginUiState(
    val isSetupMode: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

package com.example.vaultguard.ui.generator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.SecureRandom
import javax.inject.Inject

@HiltViewModel
class PasswordGeneratorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    init {
        generatePassword() // assim que abre este ecrã é criada uma password
    }

    // sempre que o utilizador altera uma definição é criada uma pass nova com a definição nova
    fun updateLength(length: Int) {
        _uiState.update { it.copy(length = length.coerceIn(8, 64)) }
        generatePassword()
    }

    fun toggleUppercase(enabled: Boolean) {
        _uiState.update { it.copy(useUppercase = enabled) }
        generatePassword()
    }

    fun toggleLowercase(enabled: Boolean) {
        _uiState.update { it.copy(useLowercase = enabled) }
        generatePassword()
    }

    fun toggleNumbers(enabled: Boolean) {
        _uiState.update { it.copy(useNumbers = enabled) }
        generatePassword()
    }

    fun toggleSymbols(enabled: Boolean) {
        _uiState.update { it.copy(useSymbols = enabled) }
        generatePassword()
    }

    // para criar uma pass temos de ver o estado atual
    fun generatePassword() {
        val state = _uiState.value
        if (!state.useUppercase && !state.useLowercase && !state.useNumbers && !state.useSymbols) {
            _uiState.update { it.copy(generatedPassword = "", error = "Select at least one character type") }
            return
        }

        // definimos os caracteres que o gerador pode utilizar
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#\$%^&*()-_=+[]{}|;:,.<>/?"

        var charPool = ""
        val requiredChars = mutableListOf<Char>() // vai guardar pelo menos um de cada tipo
        // SecureRandom em vez de Random porque é criptograficamente imprevisível -> adequado para criar passwords
        val random = SecureRandom()

        // seleciona aleatoriamente um caractere de um tipo para a lista de obrigatórios
        if (state.useUppercase) {
            charPool += upper
            requiredChars.add(upper[random.nextInt(upper.length)])
        }
        if (state.useLowercase) {
            charPool += lower
            requiredChars.add(lower[random.nextInt(lower.length)])
        }
        if (state.useNumbers) {
            charPool += numbers
            requiredChars.add(numbers[random.nextInt(numbers.length)])
        }
        if (state.useSymbols) {
            charPool += symbols
            requiredChars.add(symbols[random.nextInt(symbols.length)])
        }

        // calcula quantos caracteres faltam para chegar ao comprimento desejado
        // preenche com caracteres aleatórios do conjunto total
        val remainingLength = state.length - requiredChars.size
        for (i in 0 until remainingLength) {
            requiredChars.add(charPool[random.nextInt(charPool.length)])
        }

        // para baralhar tudo, assim os obrigatórios não aparecem primeiro
        // (primeiro maiúscula, depois minúscula, depois número, etc)
        requiredChars.shuffle(random)
        val password = requiredChars.joinToString("")

        _uiState.update { it.copy(generatedPassword = password, error = null) }
    }
}

data class GeneratorUiState(
    val length: Int = 16,
    val useUppercase: Boolean = true,
    val useLowercase: Boolean = true,
    val useNumbers: Boolean = true,
    val useSymbols: Boolean = true,
    val generatedPassword: String = "",
    val error: String? = null
)

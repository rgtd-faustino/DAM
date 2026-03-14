package com.example.vaultguard.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaultguard.data.local.entity.PasswordEntry
import com.example.vaultguard.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Hilt é uma biblioteca do Android que facilita a Injeção de Dependências
// em vez de criarmos objetos manualmente isto cria e fornece os objetos automaticamente
@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // quando o vault fragment entra neste ecrã passa um entryID, o savedStateHandle é o que recebe
    // esse valor, sobrevive a rotações de ecrã (ao contrário de um bundle normal)
    private val entryId: Int = savedStateHandle.get<Int>("entryId") ?: -1 // -1 = entrada nova

    // apenas o view model pode modificar esta variável, o fragment lê o uiState (sem underscore)
    private val _uiState = MutableStateFlow(EntryDetailUiState(isNewEntry = entryId == -1))
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        // portanto se for uma entrada quejá existe estamos a editar
        // senão estamos a editar e damos load
        if (entryId != -1) {
            loadEntry(entryId)
        }
    }


    private fun loadEntry(id: Int) {
        // começa uma corrotina (thread leve separada) para aceder à base de dados
        // (algo lento e nunca deve correr na thread principal -> a que desenha a interface)
        viewModelScope.launch {
            // antes de apanhar os dados mostrados ao user que está a carregar, o fragment mostra
            // um indicador de laoding depois
            _uiState.update { it.copy(isLoading = true) }
            // vai ao repositório apanhar a entrada, desse vai para oo DAO que depois vai à BD Room
            val entry = vaultRepository.getEntryById(id)
            if (entry != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = entry.title, // guardado em texto simples
                        username = vaultRepository.decrypt(entry.username),
                        password = vaultRepository.decrypt(entry.password),
                        notes = vaultRepository.decrypt(entry.notes)
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Entry not found") }
            }
        }
    }

    // sempre que um novo caractere for introduzido num campo o fragment chama uma destas funções e
    // atualizam o estado com o novo valor
    fun updateTitle(title: String) { _uiState.update { it.copy(title = title) } }
    fun updateUsername(username: String) { _uiState.update { it.copy(username = username) } }

    // breach count passa a ser nulo porque a pass já não é a mesma
    fun updatePassword(password: String) { _uiState.update { it.copy(password = password, breachCount = null) } }
    fun updateNotes(notes: String) { _uiState.update { it.copy(notes = notes) } }

    fun checkPasswordExposed() {
        val currentPassword = _uiState.value.password // estado atual da pass
        if (currentPassword.isBlank()) return

        // se o user quiser ver se foi comprometida então a varia´vel passa a ser true antes da
        // chamada na rede e o fragment desativa o botão e mostra um spinner
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingBreach = true) }
            val count = vaultRepository.checkPasswordExposed(currentPassword)
            _uiState.update { it.copy(isCheckingBreach = false, breachCount = count) }
        }
    }

    fun saveEntry() {
        val state = _uiState.value
        // é obrigatório haver um titulo e password
        if (state.title.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Title and Password are required") }
            return
        }

        viewModelScope.launch {
            val entry = PasswordEntry(
                // para nova adição o room cria automaticamente como definimos antes mas se
                // o user estiver a editar então usamos a entry já existente
                id = if (state.isNewEntry) 0 else entryId,
                title = state.title,
                username = state.username,
                password = state.password,
                notes = state.notes
            )

            if (state.isNewEntry) {
                vaultRepository.addEntry(entry)
            } else {
                vaultRepository.updateEntry(entry)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteEntry() {
        if (_uiState.value.isNewEntry) return
        viewModelScope.launch {
            val entry = vaultRepository.getEntryById(entryId)
            if (entry != null) {
                vaultRepository.deleteEntry(entry)
                _uiState.update { it.copy(isDeleted = true) }
            }
        }
    }
}

// estado completo do ecrã -> tudo o que o fragment tem de saber para desenhar a interface
data class EntryDetailUiState(
    val isNewEntry: Boolean = false,
    val isLoading: Boolean = false,
    val title: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val isCheckingBreach: Boolean = false,
    val breachCount: Int? = null,
    val error: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

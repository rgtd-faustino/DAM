package com.example.vaultguard.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vaultguard.data.local.entity.PasswordEntry
import com.example.vaultguard.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    // devolve um flow que emite automaticamente sempre que a BD muda
    // quando se volta do EntryDetailFragment depois de guardar uma entrada
    // a lista atualiza sozinha sem fazer nada
    // o map converte cada emissão do flow em vaultUIState
    val uiState: StateFlow<VaultUiState> = vaultRepository.getAllEntries().map { entries ->
        VaultUiState(entries = entries, isLoading = false)
        // converte o flow num stateFlow (um Flow normal só corre quando há alguém a ouvir
    // e um StateFlow tem sempre um valor atual)
    }.stateIn(
        scope = viewModelScope,
        // se o Fragment for destruído (ex: rotação) o Flow mantém-se ativo por 5 segundos
        // se o Fragment for recriado dentro desse tempo não vai buscar os dados de novo à BD
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultUiState(isLoading = true)
    )
}

data class VaultUiState(
    val entries: List<PasswordEntry> = emptyList(),
    val isLoading: Boolean = false
)

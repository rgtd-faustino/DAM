package com.example.vaultguard.ui.entry

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.vaultguard.R
import com.example.vaultguard.databinding.FragmentEntryDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint // permite ao hilt injetar dependências neste fragment
// senão não conseguia criar o view modal com as suas dependências
class EntryDetailFragment : Fragment() {

    // pode ser nulo  porque o layout pode ainda não existir
    private var _binding: FragmentEntryDetailBinding? = null
    private val binding get() = _binding!! // !! -> ter a certeza que não é nulo e se for então a app crasha

    // cria o ViewModel na primeira vez que é acedido e devolve sempre o mesmo depois
    private val viewModel: EntryDetailViewModel by viewModels()

    // inflater -> cria o ficheiro XML do layout em objetos view
    // binding.root -> view raíz do layout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEntryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    // é chamado depois do layout ser criado (configuram-se aqui event listeners e observam-se estados)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // voltar ao ecrã anterior
        }

        setupTextWatchers() // chama a função que configura os listeners de texto

        // cada botão chama uma função no ViewModel
        binding.btnSave.setOnClickListener {
            viewModel.saveEntry()
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteEntry()
        }

        binding.btnCheckBreach.setOnClickListener {
            viewModel.checkPasswordExposed()
        }

        // quando a view é destruída esta coroutine para automaticamente
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle -> só coleta dados quando o fragment está visível
            // se o utilizador mudar de app isto pausa e se volta retorna
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // sempre que o estado muda este bloco de código corre com o novo estado
                // é aqui que a UI é atualizada
                viewModel.uiState.collect { state ->
                    if (state.isSaved || state.isDeleted) {
                        findNavController().navigateUp()
                        return@collect
                    }

                    if (binding.etTitle.text.toString() != state.title) {
                        binding.etTitle.setText(state.title)
                    }
                    if (binding.etUsername.text.toString() != state.username) {
                        binding.etUsername.setText(state.username)
                    }
                    if (binding.etPassword.text.toString() != state.password) {
                        binding.etPassword.setText(state.password)
                    }
                    if (binding.etNotes.text.toString() != state.notes) {
                        binding.etNotes.setText(state.notes)
                    }

                    binding.btnDelete.visibility = if (state.isNewEntry) View.GONE else View.VISIBLE
                    binding.toolbar.title = if (state.isNewEntry) "New Entry" else "Edit Entry"

                    // enquanto verifica (isCheckingBreach = true):
                    // mostra spinner,
                    // esconde resultado,
                    // desativa botão
                    // quando termina: esconde spinner, ativa botão
                    // se breachCount não é null (houve verificação), mostra o resultado
                    // breachCount > 0 = comprometida (vermelho.
                    // breachCount == -1 = erro de rede (laranja)
                    // breachCount == 0 = segura (verde)
                    // se breachCount é null = ainda não verificou = esconde o texto de resultado
                    if (state.isCheckingBreach) {
                        binding.progressBreach.visibility = View.VISIBLE
                        binding.tvBreachResult.visibility = View.GONE
                        binding.btnCheckBreach.isEnabled = false
                    } else {
                        binding.progressBreach.visibility = View.GONE
                        binding.btnCheckBreach.isEnabled = true
                        
                        if (state.breachCount != null) {
                            binding.tvBreachResult.visibility = View.VISIBLE
                            if (state.breachCount > 0) {
                                binding.tvBreachResult.text = "Pwned ${state.breachCount} times! Change it."
                                binding.tvBreachResult.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                            } else if (state.breachCount == -1) {
                                binding.tvBreachResult.text = "Error checking breach."
                                binding.tvBreachResult.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
                            } else {
                                binding.tvBreachResult.text = "Safe! No breaches found."
                                binding.tvBreachResult.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                            }
                        } else {
                            binding.tvBreachResult.visibility = View.GONE
                        }
                    }

                    if (state.error != null) {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // o TextWatcher é uma interface com 3 métodos obrigatórios mas só queremos o afterTextChanged
    // então deixamos os outros vazios
    // cada vez que o user escreve um caractere o afterTextChanged chama o viewModel.updateTitle com o texto atual
    private fun setupTextWatchers() {
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateTitle(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateUsername(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updatePassword(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.etNotes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateNotes(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.vaultguard.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.vaultguard.R
import com.example.vaultguard.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // quando o user carrega no botão, lê o texto do campo do PIN e passa ao viewModel para validar
        binding.btnSubmit.setOnClickListener {
            val pin = binding.etPin.text.toString()
            viewModel.submitPin(pin)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // textos diferentes são mostrados dependendo se é a primeira vez ou não
                    if (state.isSetupMode) {
                        binding.tvSubtitle.text = "Setup Master PIN"
                        binding.btnSubmit.text = "Create Vault"
                    } else {
                        binding.tvSubtitle.text = "Enter Master PIN"
                        binding.btnSubmit.text = "Unlock Vault"
                    }

                    if (state.error != null) {
                        binding.tvError.text = state.error
                        binding.tvError.visibility = View.VISIBLE
                    } else {
                        binding.tvError.visibility = View.GONE
                    }

                    // quando autenticado avança para o vaultFragment
                    // (não há volta a não ser que o processo da app seja morto)
                    if (state.isAuthenticated) {
                        findNavController().navigate(R.id.action_loginFragment_to_vaultFragment)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

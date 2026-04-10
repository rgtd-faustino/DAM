package com.example.vaultguard.ui.generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
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
import com.example.vaultguard.databinding.FragmentGeneratorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PasswordGeneratorFragment : Fragment() {

    private var _binding: FragmentGeneratorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PasswordGeneratorViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    // basicamente sempre que o estado muda, atualiza-se a UI toda
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvGeneratedPassword.text = state.generatedPassword
                    binding.tvLengthValue.text = state.length.toString()
                    binding.sliderLength.value = state.length.toFloat()
                    
                    binding.switchUppercase.isChecked = state.useUppercase
                    binding.switchLowercase.isChecked = state.useLowercase
                    binding.switchNumbers.isChecked = state.useNumbers
                    binding.switchSymbols.isChecked = state.useSymbols

                    if (state.error != null) {
                        binding.tvError.text = state.error
                        binding.tvError.visibility = View.VISIBLE
                    } else {
                        binding.tvError.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.sliderLength.addOnChangeListener { _, value, _ ->
            viewModel.updateLength(value.toInt())
        }
        
        binding.switchUppercase.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleUppercase(isChecked)
        }
        binding.switchLowercase.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleLowercase(isChecked)
        }
        binding.switchNumbers.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleNumbers(isChecked)
        }
        binding.switchSymbols.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleSymbols(isChecked)
        }

        binding.btnGenerate.setOnClickListener {
            viewModel.generatePassword()
        }

        binding.btnCopy.setOnClickListener {
            val password = binding.tvGeneratedPassword.text.toString()
            if (password.isNotEmpty()) {
                // o getSystemService acede ao serviço de clipboard do android (os conteudos copiados)
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Generated Password", password)
                // e depois apanha se o texto da password criada e mete se nos conteudos copiados
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Password copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

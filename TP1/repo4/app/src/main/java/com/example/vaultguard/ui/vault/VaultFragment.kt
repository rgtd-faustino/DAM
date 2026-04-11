package com.example.vaultguard.ui.vault

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vaultguard.R
import com.example.vaultguard.databinding.FragmentVaultBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VaultFragment : Fragment() {

    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by viewModels()
    private var adapter: VaultAdapter? = null

    // cria o layout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        // FAB = Floating Action Button (o botão redondo com "+" que cria entradas, neste caso com
        // -1 porque é uma entrava nova e não é editar uma já existente)
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                VaultFragmentDirections.actionVaultFragmentToEntryDetailFragment(entryId = -1)
            )
        }

        // inicia uma coroutine ligada ao ciclo de vida da View
        // repeatOnLifecycle(STARTED) garante que só recebe atualizações enquanto o Fragment está visível
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // passa a nova lista ao adapter
                    // o DiffUtil calcula automaticamente o que mudou e anima as diferenças
                    adapter?.submitList(state.entries)
                    // o !state.isLoading é importante porque sem ele, durante o breve momento em
                    // que os dados ainda estão a carregar a lista parece vazia e
                    // a mensagem mostraria "sem entradas"
                    if (!state.isLoading && state.entries.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvEntries.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvEntries.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // configura os itens de menu definidos no XML vault_menu.xml para a toolbar
    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_generate -> {
                    findNavController().navigate(R.id.action_vaultFragment_to_passwordGeneratorFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = VaultAdapter { entry ->
            findNavController().navigate(
                VaultFragmentDirections.actionVaultFragmentToEntryDetailFragment(entryId = entry.id)
            )
        }
        binding.rvEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.vaultguard.ui.vault

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vaultguard.data.local.entity.PasswordEntry
import com.example.vaultguard.databinding.ItemVaultEntryBinding

class VaultAdapter(
    // o vaultadapter chama esta função quando é clicada
    private val onItemClick: (PasswordEntry) -> Unit
) : ListAdapter<PasswordEntry, VaultAdapter.VaultViewHolder>(EntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaultViewHolder {
        val binding = ItemVaultEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VaultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // contentor viewer
    // cada linha da lista tem um viewHolder associado (guarda referências às views da linha (
    // título, icone, etc) para não ter das procurar todas as vezes
    // é inner class para poder aceder ao onItemClick
    inner class VaultViewHolder(private val binding: ItemVaultEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        // é chamado sempre que este ViewHolder precisa de mostrar uma entrada diferente
        // só mostra o título porque o resto está encriptado
        fun bind(entry: PasswordEntry) {
            binding.tvItemTitle.text = entry.title
        }
    }
}

// quando a lista de entradas muda (adicionar, editar, apagar)
// o DiffUtil tem de saber o que mudou para só atualizar as linhas afetadas
// em vez de redesenhar a lista inteira
class EntryDiffCallback : DiffUtil.ItemCallback<PasswordEntry>() {
    override fun areItemsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry): Boolean {
        return oldItem == newItem
    }
}

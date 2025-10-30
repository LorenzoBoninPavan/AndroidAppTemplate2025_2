package com.ifpr.androidapptemplate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.ItemMarketplace // Garanta que esta classe de dados está acessível
import com.ifpr.androidapptemplate.databinding.ItemTemplateBinding // Ajuste o nome do Binding se necessário (Ex: ItemTemplateXmlBinding)

class ItemMarketplaceAdapter(
    // Lambda (função) que será chamada no HomeFragment quando houver um clique
    private val onItemClickListener: (ItemMarketplace) -> Unit
) : ListAdapter<ItemMarketplace, ItemMarketplaceAdapter.ItemViewHolder>(ItemDiffCallback()) {

    // ViewHolder: Lida com a vinculação dos dados à UI (usa View Binding)
    inner class ItemViewHolder(private val binding: ItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemMarketplace) {
            // Vincula os dados do ItemMarketplace aos TextViews no item_template.xml
            binding.itemNome.text = item.nome
            binding.itemEndereco.text = item.endereco

            // Define o Listener de clique: aciona a Lambda, passando o item clicado
            binding.root.setOnClickListener {
                onItemClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Infla o layout usando o View Binding (ItemTemplateBinding)
        val binding = ItemTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    // Classe auxiliar para otimizar as atualizações do RecyclerView
    class ItemDiffCallback : DiffUtil.ItemCallback<ItemMarketplace>() {
        override fun areItemsTheSame(oldItem: ItemMarketplace, newItem: ItemMarketplace): Boolean {
            // Compara IDs para ver se é o mesmo item
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ItemMarketplace, newItem: ItemMarketplace): Boolean {
            // Compara o conteúdo para ver se o item mudou
            return oldItem == newItem
        }
    }
}
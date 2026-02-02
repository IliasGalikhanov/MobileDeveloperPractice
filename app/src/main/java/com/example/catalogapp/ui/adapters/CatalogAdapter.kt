package com.example.catalogapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catalogapp.databinding.ItemCatalogBinding
import com.example.catalogapp.model.CatalogItem

/**
 * Adapter для отображения списка товаров в RecyclerView
 * Использует ListAdapter с DiffUtil для эффективного обновления
 */
class CatalogAdapter(
    private val onItemClick: (CatalogItem) -> Unit
) : ListAdapter<CatalogItem, CatalogAdapter.CatalogViewHolder>(CatalogItemDiffCallback()) {

    /**
     * ViewHolder для элемента списка с ViewBinding
     */
    inner class CatalogViewHolder(
        private val binding: ItemCatalogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CatalogItem) {
            binding.apply {
                itemTitle.text = item.title
                itemPrice.text = "${item.price.toInt()} ₽"
                
                // Отображение иконки избранного
                favoriteIcon.visibility = if (item.isFavorite) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Обработка клика на элемент
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
        val binding = ItemCatalogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CatalogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil.ItemCallback для эффективного сравнения элементов
     */
    private class CatalogItemDiffCallback : DiffUtil.ItemCallback<CatalogItem>() {
        override fun areItemsTheSame(oldItem: CatalogItem, newItem: CatalogItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CatalogItem, newItem: CatalogItem): Boolean {
            return oldItem == newItem
        }
    }
}

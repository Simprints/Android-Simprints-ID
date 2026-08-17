package com.simprints.feature.moduleselector.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.simprints.feature.module.selector.R

internal class ModuleSelectorAdapter(
    private val onModuleSelected: (ModuleSelectorItem.Module) -> Unit,
) : RecyclerView.Adapter<ModuleSelectorViewHolder>() {
    private val listDiffer = AsyncListDiffer(this, ModuleItemDiffCallback())

    fun submitList(list: List<ModuleSelectorItem>) {
        listDiffer.submitList(list)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ModuleSelectorViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.dialog_module_selector_item, parent, false)
        return ModuleSelectorViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ModuleSelectorViewHolder,
        position: Int,
    ) {
        holder.bindTo(listDiffer.currentList[position], onModuleSelected)
    }

    override fun getItemCount(): Int = listDiffer.currentList.size

    internal class ModuleItemDiffCallback : DiffUtil.ItemCallback<ModuleSelectorItem>() {
        override fun areItemsTheSame(
            oldItem: ModuleSelectorItem,
            newItem: ModuleSelectorItem,
        ): Boolean = when (oldItem) {
            is ModuleSelectorItem.Module if newItem is ModuleSelectorItem.Module -> oldItem.tokenizedName == newItem.tokenizedName
            is ModuleSelectorItem.NoResult if newItem is ModuleSelectorItem.NoResult -> true
            else -> false
        }

        override fun areContentsTheSame(
            oldItem: ModuleSelectorItem,
            newItem: ModuleSelectorItem,
        ): Boolean = oldItem == newItem
    }
}

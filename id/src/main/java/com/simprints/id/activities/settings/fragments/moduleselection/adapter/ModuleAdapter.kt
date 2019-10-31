package com.simprints.id.activities.settings.fragments.moduleselection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.simprints.id.R
import com.simprints.id.moduleselection.model.Module
import java.util.*

class ModuleAdapter(
    private val tracker: ModuleSelectionTracker
) : ListAdapter<Module, ModuleViewHolder>(DiffCallback) {

    fun filter(modules: List<Module>, searchTerm: String?) {
        val defaultLocale = Locale.getDefault()
        searchTerm?.toLowerCase(defaultLocale)?.let { query ->
            submitList(modules.filter {
                it.name.toLowerCase(defaultLocale).contains(query)
                // TODO: add !it.isSelected to filter once "selected modules" area is implemented
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val moduleName = getItem(position)
        holder.bindTo(moduleName, tracker)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Module>() {
        override fun areItemsTheSame(oldItem: Module, newItem: Module): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Module, newItem: Module): Boolean {
            return oldItem.name == newItem.name && oldItem.isSelected && newItem.isSelected
        }
    }

}

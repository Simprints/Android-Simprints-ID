package com.simprints.id.activities.settings.fragments.moduleselection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.simprints.id.R

class ModuleAdapter : ListAdapter<String, ModuleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val moduleName = getItem(position)
        holder.bindTo(moduleName)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.contentEquals(newItem)
        }
    }

}

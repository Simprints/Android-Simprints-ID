package com.simprints.id.activities.settings.fragments.moduleselection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.moduleselection.model.Module

open class ModuleAdapter(
    private val listener: ModuleSelectionListener
) : RecyclerView.Adapter<ModuleViewHolder>() {

    private var list = emptyList<Module>()

    fun submitList(list: List<Module>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val moduleName = list[position]
        holder.bindTo(moduleName, listener)
    }

    override fun getItemCount(): Int = list.size

}

package com.simprints.id.activities.settings.syncinformation.modulecount

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R

open class ModuleCountAdapter : RecyclerView.Adapter<ModuleCountViewHolder>() {

    private var list = emptyList<ModuleCount>()

    fun submitList(list: List<ModuleCount>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleCountViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_module_count, parent, false)
        return ModuleCountViewHolder(itemView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ModuleCountViewHolder, position: Int) {
        val moduleCount = list[position]
        holder.bind(moduleCount, position == 0)
    }


}

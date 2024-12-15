package com.simprints.feature.dashboard.settings.syncinfo.modulecount

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.simprints.feature.dashboard.R

internal open class ModuleCountAdapter : RecyclerView.Adapter<ModuleCountViewHolder>() {
    private val originalModuleCount = arrayListOf<ModuleCount>()

    fun submitList(updatedModuleCount: List<ModuleCount>) {
        val diffResult =
            DiffUtil.calculateDiff(ModuleCountDiffCallback(originalModuleCount, updatedModuleCount))
        originalModuleCount.clear()
        originalModuleCount.addAll(updatedModuleCount)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ModuleCountViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_module_count, parent, false)
        return ModuleCountViewHolder(itemView)
    }

    override fun getItemCount(): Int = originalModuleCount.size

    override fun onBindViewHolder(
        holder: ModuleCountViewHolder,
        position: Int,
    ) {
        val moduleCount = originalModuleCount[position]
        holder.bind(moduleCount, position == 0)
    }

    class ModuleCountDiffCallback(
        private val oldModules: List<ModuleCount>,
        private val newModules: List<ModuleCount>,
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ): Boolean = oldModules[oldItemPosition].name == newModules[newItemPosition].name

        override fun getOldListSize(): Int = oldModules.size

        override fun getNewListSize(): Int = newModules.size

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ): Boolean = oldModules[oldItemPosition] == newModules[newItemPosition]
    }
}

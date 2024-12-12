package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module

internal class ModuleViewHolder(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    private val txtModuleName: TextView = itemView.findViewById(R.id.txtModuleName)

    fun bindTo(
        module: Module,
        listener: ModuleSelectionListener,
    ) {
        with(txtModuleName) {
            text = module.name.value

            setOnClickListener {
                listener.onModuleSelected(module)
            }
        }
    }
}

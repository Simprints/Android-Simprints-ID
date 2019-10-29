package com.simprints.id.activities.settings.fragments.moduleselection.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.moduleselection.model.Module

class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtModuleName: TextView = itemView.findViewById(R.id.txtModuleName)

    fun bindTo(module: Module) {
        txtModuleName.text = module.name
    }

}

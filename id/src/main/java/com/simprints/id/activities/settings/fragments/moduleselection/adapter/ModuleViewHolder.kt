package com.simprints.id.activities.settings.fragments.moduleselection.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R

class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtModuleName: TextView = itemView.findViewById(R.id.txtModuleName)

    fun bindTo(moduleName: String) {
        txtModuleName.text = moduleName
    }

}

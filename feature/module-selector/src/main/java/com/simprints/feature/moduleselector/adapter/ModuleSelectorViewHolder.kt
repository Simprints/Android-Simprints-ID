package com.simprints.feature.moduleselector.adapter

import android.view.View
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.simprints.feature.module.selector.R
import com.simprints.infra.resources.R as IDR

internal class ModuleSelectorViewHolder(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    private val container: View = itemView.findViewById(R.id.module_selection_item_container)
    private val title: TextView = itemView.findViewById(R.id.module_selection_item_title)
    private val checkbox: MaterialCheckBox = itemView.findViewById(R.id.module_selection_item_checkbox)

    fun bindTo(
        item: ModuleSelectorItem,
        onModuleSelected: (ModuleSelectorItem.Module) -> Unit,
    ) {
        when (item) {
            is ModuleSelectorItem.Module -> {
                title.text = item.name
                checkbox.isVisible = true
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = item.isSelected

                container.isClickable = true
                val onClick = View.OnClickListener {
                    onModuleSelected(item)
                }
                container.setOnClickListener(onClick)
                checkbox.setOnClickListener(onClick)
            }

            ModuleSelectorItem.NoResult -> {
                title.text = itemView.context.getString(IDR.string.select_modules_no_results)
                container.isClickable = false
                container.setOnClickListener(null)
                checkbox.isInvisible = true
                checkbox.setOnClickListener(null)
            }
        }
    }
}

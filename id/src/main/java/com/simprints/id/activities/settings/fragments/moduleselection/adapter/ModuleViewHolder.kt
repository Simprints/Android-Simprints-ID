package com.simprints.id.activities.settings.fragments.moduleselection.adapter

import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.moduleselection.model.Module
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange

class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

    fun bindTo(module: Module, tracker: ModuleSelectionTracker) {
        with(checkBox) {
            text = module.name
            isChecked = module.isSelected

            onCheckedChange { _, isChecked ->
                module.isSelected = isChecked
                tracker.onSelectionStateChanged(module)
            }
        }
    }

}

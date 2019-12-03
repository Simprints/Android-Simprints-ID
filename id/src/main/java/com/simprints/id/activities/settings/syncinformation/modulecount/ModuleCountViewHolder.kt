package com.simprints.id.activities.settings.syncinformation.modulecount

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R

class ModuleCountViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val moduleNameText: TextView = itemView.findViewById(R.id.moduleNameText)
    private val moduleCountText: TextView = itemView.findViewById(R.id.moduleCountText)


    fun bind(moduleCount: ModuleCount, isFirstElementForTotalCount: Boolean) {
        moduleNameText.text = moduleCount.name
        moduleCountText.text = moduleCount.count.toString()

        if (isFirstElementForTotalCount) {
            moduleNameText.setTypeface(null, Typeface.BOLD)
            moduleCountText.setTypeface(null, Typeface.BOLD)
        }
    }
}

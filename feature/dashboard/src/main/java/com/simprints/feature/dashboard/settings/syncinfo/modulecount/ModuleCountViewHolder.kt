package com.simprints.feature.dashboard.settings.syncinfo.modulecount

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.feature.dashboard.R

internal class ModuleCountViewHolder(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    private val moduleItemIcon: ImageView = itemView.findViewById(R.id.moduleItemIcon)
    private val moduleNameText: TextView = itemView.findViewById(R.id.moduleNameText)
    private val moduleCountText: TextView = itemView.findViewById(R.id.moduleCountText)

    fun bind(
        moduleCount: ModuleCount,
        isFirstElementForTotalCount: Boolean,
    ) {
        moduleItemIcon.setImageResource(
            if (isFirstElementForTotalCount) {
                R.drawable.ic_global
            } else {
                R.drawable.ic_module
            },
        )
        moduleNameText.text = moduleCount.name
        moduleCountText.text = moduleCount.count.toString()

        if (isFirstElementForTotalCount) {
            moduleNameText.setTextColor(Color.BLACK)
            moduleCountText.setTextColor(Color.BLACK)
        }
    }
}

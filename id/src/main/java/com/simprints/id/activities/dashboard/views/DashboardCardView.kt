package com.simprints.id.activities.dashboard.views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard

open class DashboardCardView(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val image: ImageView = itemView.findViewById(R.id.dashboardCardImage)
    private val title: TextView = itemView.findViewById(R.id.dashboardCardTitle)
    private val description: TextView = itemView.findViewById(R.id.dashboardCardDescription)

    open fun bind(item: DashboardCard) {
        image.setImageResource(item.imageRes)
        title.text = item.title
        description.text = item.description
    }
}

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

    open fun bind(cardModel: DashboardCard) {
        image.setImageResource(cardModel.imageRes)
        title.text = cardModel.title
        description.text = cardModel.description
    }
}

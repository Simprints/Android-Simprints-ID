package com.simprints.id.activities.dashboard.views

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard

open class DashboardCardView(rootView: View) : RecyclerView.ViewHolder(rootView) {

    private val image: ImageView = rootView.findViewById(R.id.dashboardCardImage)
    private val title: TextView = rootView.findViewById(R.id.dashboardCardTitle)
    protected val description: TextView = rootView.findViewById(R.id.dashboardCardDescription)

    open fun bind(cardModel: DashboardCard) {
        image.setImageResource(cardModel.imageRes)
        title.text = cardModel.title
        description.text = cardModel.description
    }
}

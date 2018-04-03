package com.simprints.id.activities.dashboard

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.simprints.id.R
import com.simprints.id.activities.dashboard.models.DashboardCard
import com.simprints.id.activities.dashboard.models.DashboardLocalDbCard
import com.simprints.id.activities.dashboard.views.DashboardCardView
import com.simprints.id.activities.dashboard.views.DashboardLocalDbCardView

class DashboardCardAdapter(private val cardModels: ArrayList<DashboardCard>) :
    RecyclerView.Adapter<DashboardCardView>() {

    enum class CardViewType {
        GENERAL,
        LOCAL_DB
    }

    override fun getItemViewType(position: Int): Int {
        return if (cardModels[position] is DashboardLocalDbCard) {
            CardViewType.LOCAL_DB.ordinal
        } else {
            CardViewType.GENERAL.ordinal
        }
    }

    override fun onBindViewHolder(holder: DashboardCardView, position: Int) = holder.bind(cardModels[position])
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == CardViewType.GENERAL.ordinal) {
            DashboardCardView(LayoutInflater.from(parent.context).inflate(R.layout.activity_dashboard_card, parent, false))
        } else {
            DashboardLocalDbCardView(LayoutInflater.from(parent.context).inflate(R.layout.activity_dashboard_localdb_card, parent, false))
        }

    override fun getItemCount() = cardModels.size
}

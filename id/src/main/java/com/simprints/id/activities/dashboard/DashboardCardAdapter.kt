package com.simprints.id.activities.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.viewModels.CardViewModel
import com.simprints.id.activities.dashboard.viewModels.DashboardSyncCardViewModel
import com.simprints.id.activities.dashboard.views.DashboardCardView
import com.simprints.id.activities.dashboard.views.DashboardSyncCardView

class DashboardCardAdapter(private val cardModels: ArrayList<CardViewModel>) :
    RecyclerView.Adapter<DashboardCardView>() {

    enum class CardViewType {
        GENERAL,
        SYNC
    }

    override fun getItemViewType(position: Int): Int {
        return if (cardModels[position] is DashboardSyncCardViewModel) {
            CardViewType.SYNC.ordinal
        } else {
            CardViewType.GENERAL.ordinal
        }
    }

    override fun onBindViewHolder(holder: DashboardCardView, position: Int) = holder.bind(cardModels[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == CardViewType.GENERAL.ordinal) {
            DashboardCardView(LayoutInflater.from(parent.context).inflate(R.layout.activity_dashboard_card, parent, false))
        } else {
            DashboardSyncCardView(LayoutInflater.from(parent.context).inflate(R.layout.activity_dashboard_sync_card, parent, false))
        }

    override fun getItemCount() = cardModels.size


    override fun onViewAttachedToWindow(holder: DashboardCardView) {
        super.onViewAttachedToWindow(holder)
        holder.onAppear()
    }

    override fun onViewDetachedFromWindow(holder: DashboardCardView) {
        super.onViewDetachedFromWindow(holder)
        holder.onDisappear()
    }
}

package com.simprints.id.activities.dashboard.cards.sync

import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView

interface DashboardSyncCardDisplayer {

    fun initViews(cardViews: ViewGroup)
    fun displayState(syncCardState: DashboardSyncCardState)
}

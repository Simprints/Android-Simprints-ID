package com.simprints.id.activities.dashboard.cards.sync

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView

interface DashboardSyncCardDisplayer {

    fun initRoot(root: LinearLayout)
    fun displayState(syncCardState: DashboardSyncCardState)
}

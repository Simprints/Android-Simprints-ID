package com.simprints.id.activities.dashboard.cards.sync

import android.widget.LinearLayout

interface DashboardSyncCardDisplayer {

    fun initRoot(syncCardsRootLayout: LinearLayout)
    fun displayState(syncCardState: DashboardSyncCardState)
}

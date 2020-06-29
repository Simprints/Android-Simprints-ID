package com.simprints.id.activities.dashboard.cards.sync

import android.content.Context
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import com.simprints.core.livedata.LiveDataEvent

interface DashboardSyncCardDisplayer {

    val userWantsToSelectAModule: LiveData<LiveDataEvent>
    val userWantsToOpenSettings: LiveData<LiveDataEvent>
    val userWantsToSync: LiveData<LiveDataEvent>

    fun initRoot(syncCardsRootLayout: LinearLayout, context: Context)
    fun displayState(syncCardState: DashboardSyncCardState)
    suspend fun startTickerToUpdateLastSyncText()
    fun stopOngoingTickerToUpdateLastSyncText()
}

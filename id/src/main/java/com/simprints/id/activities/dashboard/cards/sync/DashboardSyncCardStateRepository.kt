package com.simprints.id.activities.dashboard.cards.sync

import androidx.lifecycle.LiveData

interface DashboardSyncCardStateRepository {

    val syncCardStateLiveData: LiveData<DashboardSyncCardState>

    suspend fun syncIfRequired()
}

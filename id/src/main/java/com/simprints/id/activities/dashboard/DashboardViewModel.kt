package com.simprints.id.activities.dashboard

import androidx.lifecycle.ViewModel
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository

class DashboardViewModel(private val dashboardSyncCardStateRepository: DashboardSyncCardStateRepository) : ViewModel() {


    var syncCardStateLiveData = dashboardSyncCardStateRepository.syncCardStateLiveData

    fun syncIfRequired() = dashboardSyncCardStateRepository.syncIfRequired()
}

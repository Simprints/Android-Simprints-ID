package com.simprints.id.activities.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.activities.consent.ConsentViewModel
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.tools.AndroidResourcesHelper

class DashboardViewModelFactory(val peopleSyncManager: PeopleSyncManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            DashboardViewModel(peopleSyncManager) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}

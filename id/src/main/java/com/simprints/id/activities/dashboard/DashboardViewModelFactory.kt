package com.simprints.id.activities.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager

class DashboardViewModelFactory(
    private val peopleSyncManager: PeopleSyncManager,
    private val projectDetailsRepository: DashboardProjectDetailsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            DashboardViewModel(
                peopleSyncManager,
                projectDetailsRepository
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}

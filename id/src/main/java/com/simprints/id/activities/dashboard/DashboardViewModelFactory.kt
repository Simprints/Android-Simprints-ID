package com.simprints.id.activities.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository

class DashboardViewModelFactory(
    private val dashboardProjectDetailsRepository: DashboardProjectDetailsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            DashboardViewModel(dashboardProjectDetailsRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}

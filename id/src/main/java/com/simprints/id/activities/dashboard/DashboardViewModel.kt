package com.simprints.id.activities.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectWrapper
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository

class DashboardViewModel(private val repository: DashboardProjectDetailsRepository) : ViewModel() {

    suspend fun getProjectDetails(): LiveData<DashboardProjectWrapper> {
        return repository.getProjectDetails()
    }

}

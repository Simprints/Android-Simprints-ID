package com.simprints.id.activities.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val projectDetailsRepository: DashboardProjectDetailsRepository,
    private val syncCardStateRepository: DashboardSyncCardStateRepository,
    private val dailyActivityRepository: DashboardDailyActivityRepository
) : ViewModel() {

    var syncCardStateLiveData = syncCardStateRepository.syncCardStateLiveData

    private val projectCardStateLiveData = MutableLiveData<DashboardProjectState>()
    private val dailyActivityCardStateLiveData = MutableLiveData<DashboardDailyActivityState>()

    fun syncIfRequired() = syncCardStateRepository.syncIfRequired()

    init {
        loadProjectDetails()
        loadDailyActivity()
    }

    fun getProjectDetails(): LiveData<DashboardProjectState> = projectCardStateLiveData

    fun getDailyActivity(): LiveData<DashboardDailyActivityState> = dailyActivityCardStateLiveData

    private fun loadProjectDetails() {
        viewModelScope.launch {
            val projectDetails = projectDetailsRepository.getProjectDetails()
            projectCardStateLiveData.postValue(projectDetails)
        }
    }

    private fun loadDailyActivity() = dailyActivityRepository.getDailyActivity()

}

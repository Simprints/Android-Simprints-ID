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
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.canSyncDataToSimprints
import com.simprints.infra.config.domain.models.isEventDownSyncAllowed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val projectDetailsRepository: DashboardProjectDetailsRepository,
    private val syncCardStateRepository: DashboardSyncCardStateRepository,
    private val dailyActivityRepository: DashboardDailyActivityRepository,
    private val configManager: ConfigManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    val consentRequiredLiveData = MutableLiveData<Boolean>()
    val syncToBFSIDAllowed = MutableLiveData<Boolean>()
    val dailyActivity = MutableLiveData<DashboardDailyActivityState>()
    var syncCardStateLiveData = syncCardStateRepository.syncCardStateLiveData

    private val projectCardStateLiveData = MutableLiveData<DashboardProjectState>()


    init {
        load()
    }

    fun syncIfRequired() {
        viewModelScope.launch { syncCardStateRepository.syncIfRequired() }
    }

    fun getProjectDetails(): LiveData<DashboardProjectState> = projectCardStateLiveData

    private fun load() {
        viewModelScope.launch(dispatcher) {
            val projectDetails = projectDetailsRepository.getProjectDetails()
            val configuration = configManager.getProjectConfiguration()
            dailyActivity.postValue(dailyActivityRepository.getDailyActivity())
            projectCardStateLiveData.postValue(projectDetails)
            consentRequiredLiveData.postValue(configuration.consent.collectConsent)
            syncToBFSIDAllowed.postValue(configuration.canSyncDataToSimprints() || configuration.isEventDownSyncAllowed())
        }
    }

}

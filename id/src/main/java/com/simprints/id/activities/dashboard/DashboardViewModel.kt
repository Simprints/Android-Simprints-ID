package com.simprints.id.activities.dashboard

import androidx.lifecycle.*
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncState
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(
    peopleSyncManager: PeopleSyncManager,
    private val projectDetailsRepository: DashboardProjectDetailsRepository
) : ViewModel() {

    //private val peopleSyncStateLiveData = peopleSyncManager.getLastSyncState()
    private val peopleSyncStateLiveData = MutableLiveData<PeopleSyncState>()
    private val projectCardStateLiveData = MutableLiveData<DashboardProjectState>()

    val syncCardState = Transformations.map(peopleSyncStateLiveData) {
        DashboardSyncCardState.SyncProgress(Date(), 10, 100)
    }

    init {
        loadProjectDetails()
    }

    fun getProjectDetails(): LiveData<DashboardProjectState> = projectCardStateLiveData

    //StopShip: use only for debug - it will be gone when the business logic is wired up to the UI
    fun emit() {
        peopleSyncStateLiveData.value = PeopleSyncState("", 10, 100, emptyList(), emptyList())
    }

    private fun loadProjectDetails() {
        viewModelScope.launch {
            val projectDetails = projectDetailsRepository.getProjectDetails()
            projectCardStateLiveData.postValue(projectDetails)
        }
    }

}

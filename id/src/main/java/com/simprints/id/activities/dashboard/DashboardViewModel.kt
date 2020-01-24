package com.simprints.id.activities.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectWrapper
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncState
import java.util.*

class DashboardViewModel(
    projectRepository: ProjectRepository,
    loginInfoManager: LoginInfoManager,
    preferencesManager: PreferencesManager,
    peopleSyncManager: PeopleSyncManager
) : ViewModel() {

    private val repository = DashboardProjectDetailsRepository(
        projectRepository, loginInfoManager, preferencesManager
    )

    //private val peopleSyncStateLiveData = peopleSyncManager.getLastSyncState()
    private val peopleSyncStateLiveData = MutableLiveData<PeopleSyncState>()

    val syncCardState = Transformations.map(peopleSyncStateLiveData) {
        DashboardSyncCardState.SyncProgress(Date(), 10, 100)
    }

    suspend fun getProjectDetails(): LiveData<DashboardProjectWrapper> {
        return repository.getProjectDetails()
    }

    //StopShip: use only for debug - it will be gone when the business logic is wired up to the UI
    fun emit() {
        peopleSyncStateLiveData.value = PeopleSyncState("", 10, 100, emptyList(), emptyList())
    }

}

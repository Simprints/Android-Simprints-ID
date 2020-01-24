package com.simprints.id.activities.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager

class DashboardViewModelFactory(
    private val projectRepository: ProjectRepository,
    private val loginInfoManager: LoginInfoManager,
    private val preferencesManager: PreferencesManager,
    private val peopleSyncManager: PeopleSyncManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            DashboardViewModel(
                projectRepository,
                loginInfoManager,
                preferencesManager,
                peopleSyncManager
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}

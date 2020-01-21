package com.simprints.id.activities.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectWrapper
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager

class DashboardViewModel(
    projectRepository: ProjectRepository,
    loginInfoManager: LoginInfoManager,
    preferencesManager: PreferencesManager
) : ViewModel() {

    private val repository = DashboardProjectDetailsRepository(
        projectRepository, loginInfoManager, preferencesManager
    )

    suspend fun getProjectDetails(): LiveData<DashboardProjectWrapper> {
        return repository.getProjectDetails()
    }

}

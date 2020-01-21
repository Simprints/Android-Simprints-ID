package com.simprints.id.activities.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectWrapper
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager

class DashboardViewModel(
    private val projectRepository: ProjectRepository,
    private val loginInfoManager: LoginInfoManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val projectDetailsLiveData = MutableLiveData<DashboardProjectWrapper>()

    suspend fun getProjectDetails(): LiveData<DashboardProjectWrapper> {
        val title = projectRepository.loadAndRefreshCache(
            loginInfoManager.getSignedInProjectIdOrEmpty()
        )?.name ?: ""
        val lastUser = preferencesManager.lastUserUsed
        val lastScanner = preferencesManager.lastScannerUsed

        return projectDetailsLiveData.apply {
            postValue(DashboardProjectWrapper(title, lastUser, lastScanner))
        }
    }

}

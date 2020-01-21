package com.simprints.id.activities.dashboard.cards.project.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectWrapper
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager

class DashboardProjectDetailsRepositoryImpl(
    private val projectRepository: ProjectRepository,
    private val loginInfoManager: LoginInfoManager,
    private val preferencesManager: PreferencesManager
) : DashboardProjectDetailsRepository {

    private val projectDetailsLiveData = MutableLiveData<DashboardProjectWrapper>()

    override suspend fun getProjectDetails(): LiveData<DashboardProjectWrapper> {
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        val cachedProject = projectRepository.loadFromCache(projectId)

        val projectName = cachedProject?.name
            ?: projectRepository.loadFromRemoteAndRefreshCache(projectId)?.name
            ?: ""
        val lastUser = preferencesManager.lastUserUsed
        val lastScanner = preferencesManager.lastScannerUsed

        return projectDetailsLiveData.apply {
            postValue(DashboardProjectWrapper(projectName, lastUser, lastScanner))
        }
    }

}

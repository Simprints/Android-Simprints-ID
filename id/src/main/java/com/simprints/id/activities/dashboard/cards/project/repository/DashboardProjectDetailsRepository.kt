package com.simprints.id.activities.dashboard.cards.project.repository

import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.infra.login.LoginManager

class DashboardProjectDetailsRepository(
    private val projectRepository: ProjectRepository,
    private val loginManager: LoginManager,
    private val preferencesManager: PreferencesManager
) {

    suspend fun getProjectDetails(): DashboardProjectState {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()
        val cachedProject = projectRepository.loadFromCache(projectId)

        val projectName = cachedProject?.name
            ?: projectRepository.loadFromRemoteAndRefreshCache(projectId)?.name
            ?: ""
        val lastUser = preferencesManager.lastUserUsed
        val lastScanner = preferencesManager.lastScannerUsed

        return DashboardProjectState(projectName, lastUser, lastScanner)
    }

}

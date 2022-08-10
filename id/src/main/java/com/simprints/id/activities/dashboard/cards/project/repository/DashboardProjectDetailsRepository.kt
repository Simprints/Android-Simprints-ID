package com.simprints.id.activities.dashboard.cards.project.repository

import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager

class DashboardProjectDetailsRepository(
    private val configManager: ConfigManager,
    private val loginManager: LoginManager,
    private val preferencesManager: PreferencesManager
) {

    suspend fun getProjectDetails(): DashboardProjectState {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()
        val cachedProject = configManager.getProject(projectId)

        val lastUser = preferencesManager.lastUserUsed
        val lastScanner = preferencesManager.lastScannerUsed

        return DashboardProjectState(cachedProject.name, lastUser, lastScanner)
    }

}

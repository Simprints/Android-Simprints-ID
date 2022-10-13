package com.simprints.id.activities.dashboard.cards.project.repository

import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import javax.inject.Inject

class DashboardProjectDetailsRepository @Inject constructor(
    private val configManager: ConfigManager,
    private val loginManager: LoginManager,
    private val recentUserActivityManager: RecentUserActivityManager
) {

    suspend fun getProjectDetails(): DashboardProjectState {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()
        val cachedProject = configManager.getProject(projectId)
        val recentUserActivity = recentUserActivityManager.getRecentUserActivity()

        val lastUser = recentUserActivity.lastUserUsed
        val lastScanner = recentUserActivity.lastScannerUsed

        return DashboardProjectState(cachedProject.name, lastUser, lastScanner)
    }

}

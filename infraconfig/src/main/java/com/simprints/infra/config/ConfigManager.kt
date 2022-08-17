package com.simprints.infra.config

import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration

interface ConfigManager {
    /**
     * fetch the latest state of the project and save it locally
     */
    suspend fun refreshProject(projectId: String): Project

    /**
     * get the project locally or if not present fetch it remotely
     */
    suspend fun getProject(projectId: String): Project

    /**
     * get the project configuration locally
     */
    suspend fun getConfiguration(): ProjectConfiguration

    /**
     * fetch the latest configuration of the project and save it locally
     */
    suspend fun refreshConfiguration(projectId: String): ProjectConfiguration

    fun scheduleSyncConfiguration()
    fun cancelScheduledSyncConfiguration()
}

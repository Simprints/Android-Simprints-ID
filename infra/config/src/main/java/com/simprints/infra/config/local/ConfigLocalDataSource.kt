package com.simprints.infra.config.local

import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.ProjectConfiguration

internal interface ConfigLocalDataSource {

    suspend fun saveProject(project: Project)

    suspend fun getProject(): Project

    suspend fun clearProject()

    suspend fun saveProjectConfiguration(config: ProjectConfiguration)

    suspend fun getProjectConfiguration(): ProjectConfiguration

    suspend fun clearProjectConfiguration()

    suspend fun getDeviceConfiguration(): DeviceConfiguration

    suspend fun updateDeviceConfiguration(update: suspend (t: DeviceConfiguration) -> DeviceConfiguration)

    suspend fun clearDeviceConfiguration()

    fun hasPrivacyNoticeFor(projectId: String, language: String): Boolean

    fun storePrivacyNotice(projectId: String,language: String, content: String)

    fun getPrivacyNotice(projectId: String, language: String): String

    fun deletePrivacyNotices()
}

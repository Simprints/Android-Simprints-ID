package com.simprints.infra.config.store.remote

import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.ProjectWithConfig

internal interface ConfigRemoteDataSource {
    suspend fun getProject(projectId: String): ProjectWithConfig

    suspend fun getPrivacyNotice(
        projectId: String,
        fileId: String,
    ): String

    suspend fun getDeviceState(
        projectId: String,
        deviceId: String,
        previousInstructionId: String,
    ): DeviceState
}

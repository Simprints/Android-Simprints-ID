package com.simprints.infra.sync.devicestate

import com.simprints.core.DeviceID
import com.simprints.infra.backendapi.BackendApiClient
import com.simprints.infra.sync.devicestate.remote.DeviceStatusRemoteInterface
import com.simprints.infra.sync.devicestate.remote.toApi
import javax.inject.Inject

internal class ReportDeviceDataStateUseCase @Inject constructor(
    private val deviceStateDataTracker: DeviceStateDataTracker,
    private val backendApiClient: BackendApiClient,
    @param:DeviceID private val deviceId: String,
) {
    /**
     * @return `false` if there is no signed-in project to report for, `true` once the report is accepted.
     * @throws Throwable if the backend call fails.
     */
    suspend operator fun invoke(): Boolean {
        val state = deviceStateDataTracker.getCurrentDeviceDataState()
        val projectId = state.projectId ?: return false

        backendApiClient
            .executeCall(DeviceStatusRemoteInterface::class) {
                it.postDeviceStatus(projectId, deviceId, state.toApi())
            }.getOrThrow()
        return true
    }
}

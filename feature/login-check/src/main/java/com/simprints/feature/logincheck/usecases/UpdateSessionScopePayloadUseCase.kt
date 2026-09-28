package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import javax.inject.Inject

internal class UpdateSessionScopePayloadUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val deviceStateDataTracker: DeviceStateDataTracker,
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke() {
        val configUpdatedAt = configRepository.getProjectConfiguration().updatedAt
        val recordCount = deviceStateDataTracker.getRecordCount()
        val sessionScope = eventRepository.getCurrentSessionScope()

        val updatedScope = sessionScope.copy(
            payload = sessionScope.payload.copy(
                projectConfigurationUpdatedAt = configUpdatedAt,
                databaseInfo = sessionScope.payload.databaseInfo.copy(
                    recordCount = recordCount,
                ),
            ),
        )

        eventRepository.saveSessionScope(updatedScope)
    }
}

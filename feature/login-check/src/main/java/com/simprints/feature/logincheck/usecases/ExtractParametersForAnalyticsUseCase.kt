package com.simprints.feature.logincheck.usecases

import com.simprints.core.DeviceID
import com.simprints.core.tools.exceptions.ignoreException
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class ExtractParametersForAnalyticsUseCase @Inject constructor(
    @DeviceID private val deviceId: String,
    private val eventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(action: ActionRequest) = with(action) {
        ignoreException {
            if (this is ActionRequest.FlowAction) {
                Simber.tag(AnalyticsUserProperties.USER_ID, true).i(userId.toString())
                Simber.tag(AnalyticsUserProperties.PROJECT_ID).i(projectId)
                Simber.tag(AnalyticsUserProperties.MODULE_ID).i(moduleId.toString())
                Simber.tag(AnalyticsUserProperties.DEVICE_ID).i(deviceId)
            }
            Simber.i(eventRepository.getCurrentSessionScope().id)
        }
    }
}

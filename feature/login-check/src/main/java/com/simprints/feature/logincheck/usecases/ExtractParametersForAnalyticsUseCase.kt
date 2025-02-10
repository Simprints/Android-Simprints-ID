package com.simprints.feature.logincheck.usecases

import com.simprints.core.DeviceID
import com.simprints.core.tools.exceptions.ignoreException
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

internal class ExtractParametersForAnalyticsUseCase @Inject constructor(
    @DeviceID private val deviceId: String,
) {
    suspend operator fun invoke(action: ActionRequest) = with(action) {
        ignoreException {
            if (this is ActionRequest.FlowAction) {
                Simber.setUserProperty(AnalyticsUserProperties.USER_ID, userId.toString())
                Simber.setUserProperty(AnalyticsUserProperties.PROJECT_ID, projectId)
                Simber.setUserProperty(AnalyticsUserProperties.MODULE_ID, moduleId.toString())
                Simber.setUserProperty(AnalyticsUserProperties.DEVICE_ID, deviceId)
            }
        }
    }
}

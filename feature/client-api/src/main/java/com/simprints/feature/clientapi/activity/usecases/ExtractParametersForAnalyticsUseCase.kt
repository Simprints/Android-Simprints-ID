package com.simprints.feature.clientapi.activity.usecases

import com.simprints.core.DeviceID
import com.simprints.core.tools.exceptions.ignoreException
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class ExtractParametersForAnalyticsUseCase @Inject constructor(
    @DeviceID private val deviceId: String,
    private val clientSessionManager: ClientSessionManager,
) {
    suspend operator fun invoke(action: ActionRequest) = with(action) {
        ignoreException {
            if (this is ActionRequest.FlowAction) {
                Simber.i(userId)
                Simber.i(projectId)
                Simber.i(moduleId)
                Simber.i(deviceId)
            }
            Simber.i(clientSessionManager.getCurrentSessionId())
        }
    }
}

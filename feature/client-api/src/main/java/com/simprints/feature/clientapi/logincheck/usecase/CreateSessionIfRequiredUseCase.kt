package com.simprints.feature.clientapi.logincheck.usecase

import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.IntegrationConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.infra.events.event.domain.models.IntentParsingEvent
import javax.inject.Inject

internal class CreateSessionIfRequiredUseCase @Inject constructor(
    private val clientSessionManager: ClientSessionManager,
) {

    suspend operator fun invoke(action: String) {
        val actionName = action.substringAfterLast('.')
        if (actionName == IntegrationConstants.ACTION_CONFIRM_IDENTITY || actionName == IntegrationConstants.ACTION_ENROL_LAST_BIOMETRICS) {
            return
        }
        val integrationInfo = when (action.substringBeforeLast('.')) {
            OdkConstants.PACKAGE_NAME -> IntentParsingEvent.IntentParsingPayload.IntegrationInfo.ODK
            CommCareConstants.PACKAGE_NAME -> IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
            else -> IntentParsingEvent.IntentParsingPayload.IntegrationInfo.STANDARD
        }

        clientSessionManager.createSession(integrationInfo)
    }
}

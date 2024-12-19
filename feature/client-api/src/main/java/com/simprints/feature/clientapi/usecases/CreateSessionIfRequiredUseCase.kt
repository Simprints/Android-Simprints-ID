package com.simprints.feature.clientapi.usecases

import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.infra.events.event.domain.models.IntentParsingEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionConstants
import javax.inject.Inject

internal class CreateSessionIfRequiredUseCase @Inject constructor(
    private val coreEventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
) {
    suspend operator fun invoke(action: String): Boolean {
        val actionName = action.substringAfterLast('.')
        if (actionName == ActionConstants.ACTION_CONFIRM_IDENTITY || actionName == ActionConstants.ACTION_ENROL_LAST_BIOMETRICS) {
            return false
        }
        val integrationInfo = when (action.substringBeforeLast('.')) {
            OdkConstants.PACKAGE_NAME -> IntentParsingEvent.IntentParsingPayload.IntegrationInfo.ODK
            CommCareConstants.PACKAGE_NAME -> IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
            else -> IntentParsingEvent.IntentParsingPayload.IntegrationInfo.STANDARD
        }

        coreEventRepository
            .createSession()
            .also { Simber.setUserProperty(LoggingConstants.CrashReportingCustomKeys.SESSION_ID, it.id) }
        coreEventRepository.addOrUpdateEvent(IntentParsingEvent(timeHelper.now(), integrationInfo))
        return true
    }
}

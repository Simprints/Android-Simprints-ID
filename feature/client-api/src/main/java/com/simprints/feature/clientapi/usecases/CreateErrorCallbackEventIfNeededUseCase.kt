package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExternalScope
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [MS-719] Some errors (i.e.: LICENSE_MISSING, LICENSE_INVALID) are bypassing the orchestrator view
 * model which is responsible for creating the callback events. Alert screens are currently
 * not calling the orchestrator for error handling, and this use case is responsible for creating
 * the necessary error callbacks events from the Alert Screen
 */
internal class CreateErrorCallbackEventIfNeededUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    private val timeHelper: TimeHelper,
    @ExternalScope private val externalScope: CoroutineScope,
) {
    operator fun invoke(errorReason: AppErrorReason) {
        val event: ErrorCallbackEvent? = when (errorReason) {
            AppErrorReason.LICENSE_MISSING,
            AppErrorReason.LICENSE_INVALID -> buildErrorCallbackEvent(errorReason)

            AppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
            AppErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
            AppErrorReason.GUID_NOT_FOUND_ONLINE,
            AppErrorReason.GUID_NOT_FOUND_OFFLINE,
            AppErrorReason.BLUETOOTH_NOT_SUPPORTED,
            AppErrorReason.LOGIN_NOT_COMPLETE,
            AppErrorReason.UNEXPECTED_ERROR,
            AppErrorReason.ROOTED_DEVICE,
            AppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
            AppErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
            AppErrorReason.FACE_CONFIGURATION_ERROR,
            AppErrorReason.BACKEND_MAINTENANCE_ERROR,
            AppErrorReason.PROJECT_PAUSED,
            AppErrorReason.BLUETOOTH_NO_PERMISSION,
            AppErrorReason.PROJECT_ENDING,
            AppErrorReason.AGE_GROUP_NOT_SUPPORTED -> null
        }

        event?.let {
            externalScope.launch { eventRepository.addOrUpdateEvent(event) }
        }
    }

    private fun buildErrorCallbackEvent(reason: AppErrorReason) = ErrorCallbackEvent(
        createdAt = timeHelper.now(),
        reason = ErrorCallbackEvent.ErrorCallbackPayload.Reason.fromAppResponseErrorReasonToEventReason(
            reason
        ),
    )
}

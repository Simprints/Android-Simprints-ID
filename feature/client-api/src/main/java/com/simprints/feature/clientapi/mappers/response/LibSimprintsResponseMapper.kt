package com.simprints.feature.clientapi.mappers.response

import android.os.Bundle
import androidx.core.os.bundleOf
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import com.simprints.moduleapi.app.responses.IAppErrorReason
import javax.inject.Inject

internal class LibSimprintsResponseMapper @Inject constructor() {

    operator fun invoke(response: ActionResponse): Bundle = when (response) {

        is ActionResponse.EnrolActionResponse -> bundleOf(
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK to true,
            Constants.SIMPRINTS_REGISTRATION to Registration(response.enrolledGuid),
        ).appendCoSyncData(response.eventsJson, response.subjectActions)

        is ActionResponse.IdentifyActionResponse -> bundleOf(
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK to true,
            Constants.SIMPRINTS_IDENTIFICATIONS to response.identifications
                .map { Identification(it.guid, it.confidenceScore, Tier.valueOf(it.tier.name)) }
                .toTypedArray(),
        ).appendCoSyncData(response.eventsJson)

        is ActionResponse.ConfirmActionResponse -> bundleOf(
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK to true,
        ).appendCoSyncData(response.eventsJson)

        is ActionResponse.VerifyActionResponse -> bundleOf(
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK to true,
            Constants.SIMPRINTS_VERIFICATION to Verification(
                response.matchResult.confidenceScore,
                Tier.valueOf(response.matchResult.tier.name),
                response.matchResult.guid,
            ),
        ).appendCoSyncData(response.eventsJson)

        is ActionResponse.ExitFormActionResponse -> bundleOf(
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK to true,
            Constants.SIMPRINTS_REFUSAL_FORM to RefusalForm(response.reason, response.extraText),
        ).appendCoSyncData(response.eventsJson)

        is ActionResponse.ErrorActionResponse -> bundleOf(
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK to response.flowCompleted,
            RESULT_CODE_OVERRIDE to response.reason.libSimprintsResultCode()
        ).appendCoSyncData(response.eventsJson)
    }

    private fun Bundle.appendCoSyncData(events: String?, actions: String? = null) = apply {
        events?.let { putString(Constants.SIMPRINTS_COSYNC_EVENT, it) }
        actions?.let { putString(Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS, it) }
    }

    private fun IAppErrorReason.libSimprintsResultCode() = when (this) {
        IAppErrorReason.UNEXPECTED_ERROR -> Constants.SIMPRINTS_UNEXPECTED_ERROR
        IAppErrorReason.ROOTED_DEVICE -> Constants.SIMPRINTS_ROOTED_DEVICE
        IAppErrorReason.LOGIN_NOT_COMPLETE -> Constants.SIMPRINTS_LOGIN_NOT_COMPLETE
        IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> Constants.SIMPRINTS_INVALID_PROJECT_ID
        IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN -> Constants.SIMPRINTS_INVALID_USER_ID
        IAppErrorReason.GUID_NOT_FOUND_ONLINE -> Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE
        IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED -> Constants.SIMPRINTS_ENROLMENT_LAST_BIOMETRICS_FAILED
        IAppErrorReason.BLUETOOTH_NOT_SUPPORTED -> Constants.SIMPRINTS_BLUETOOTH_NOT_SUPPORTED
        IAppErrorReason.BLUETOOTH_NO_PERMISSION -> Constants.SIMPRINTS_BLUETOOTH_NO_PERMISSION
        IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR -> Constants.SIMPRINTS_FINGERPRINT_CONFIGURATION_ERROR
        IAppErrorReason.FACE_CONFIGURATION_ERROR -> Constants.SIMPRINTS_FACE_CONFIGURATION_ERROR
        IAppErrorReason.FACE_LICENSE_MISSING -> Constants.SIMPRINTS_FACE_LICENSE_MISSING
        IAppErrorReason.FACE_LICENSE_INVALID -> Constants.SIMPRINTS_FACE_LICENSE_INVALID
        IAppErrorReason.BACKEND_MAINTENANCE_ERROR -> Constants.SIMPRINTS_BACKEND_MAINTENANCE_ERROR
        IAppErrorReason.PROJECT_PAUSED -> Constants.SIMPRINTS_PROJECT_PAUSED
        IAppErrorReason.PROJECT_ENDING -> Constants.SIMPRINTS_PROJECT_ENDING

        /*
        TODO incorporate these error codes into the client api
        INVALID_CLIENT_REQUEST -> Constants.SIMPRINTS_INVALID_INTENT_ACTION
        INVALID_METADATA -> Constants.SIMPRINTS_INVALID_METADATA
        INVALID_MODULE_ID -> Constants.SIMPRINTS_INVALID_MODULE_ID
        INVALID_PROJECT_ID -> Constants.SIMPRINTS_INVALID_PROJECT_ID
        INVALID_SELECTED_ID -> Constants.SIMPRINTS_INVALID_SELECTED_ID
        INVALID_SESSION_ID -> Constants.SIMPRINTS_INVALID_SESSION_ID
        INVALID_USER_ID -> Constants.SIMPRINTS_INVALID_USER_ID
        INVALID_VERIFY_ID -> Constants.SIMPRINTS_INVALID_VERIFY_GUID
        INVALID_STATE_FOR_INTENT_ACTION -> Constants.SIMPRINTS_INVALID_STATE_FOR_INTENT_ACTION
        */
    }

    companion object {
        internal const val RESULT_CODE_OVERRIDE = "result_code_override"
    }
}

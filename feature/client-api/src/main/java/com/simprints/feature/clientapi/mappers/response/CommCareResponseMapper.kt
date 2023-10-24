package com.simprints.feature.clientapi.mappers.response

import android.os.Bundle
import androidx.core.os.bundleOf
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import javax.inject.Inject

internal class CommCareResponseMapper @Inject constructor() {

    operator fun invoke(response: ActionResponse): Bundle = when (response) {
        is ActionResponse.EnrolActionResponse -> bundleOf(
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
            CommCareConstants.REGISTRATION_GUID_KEY to Registration(response.enrolledGuid),
        ).appendCoSyncData(response.eventsJson, response.subjectActions).toCommCareBundle()

        /**
         * CommCare expect Identification result in LibSimprints 1.0.12 format.
         * That's why it is being returned in a different way from others (not inside [CommCareConstants.COMMCARE_BUNDLE_KEY]).
         */
        is ActionResponse.IdentifyActionResponse -> bundleOf(
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_IDENTIFICATIONS to response.identifications
                .map { Identification(it.guid, it.confidenceScore, Tier.valueOf(it.tier.name)) }
                .toTypedArray()
        ).appendCoSyncData(response.eventsJson)

        is ActionResponse.ConfirmActionResponse -> bundleOf(
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
        ).appendCoSyncData(response.eventsJson).toCommCareBundle()

        is ActionResponse.VerifyActionResponse -> bundleOf(
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
            CommCareConstants.VERIFICATION_GUID_KEY to response.matchResult.guid,
            CommCareConstants.VERIFICATION_CONFIDENCE_KEY to response.matchResult.confidenceScore.toString(),
            CommCareConstants.VERIFICATION_TIER_KEY to response.matchResult.tier.name,
        ).appendCoSyncData(response.eventsJson).toCommCareBundle()

        is ActionResponse.ExitFormActionResponse -> bundleOf(
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
            CommCareConstants.EXIT_REASON to response.reason,
            CommCareConstants.EXIT_EXTRA to response.extraText,
        ).appendCoSyncData(response.eventsJson).toCommCareBundle()

        is ActionResponse.ErrorActionResponse -> bundleOf(
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to response.flowCompleted.toString(),
        ).appendCoSyncData(response.eventsJson).toCommCareBundle()
    }

    private fun Bundle.appendCoSyncData(events: String?, actions: String? = null) = apply {
        events?.let { putString(CommCareConstants.SIMPRINTS_EVENTS, it) }
        actions?.let { putString(CommCareConstants.SIMPRINTS_SUBJECT_ACTIONS, it) }
    }

    // Based on the documentation, we are supposed to send either [CommCareConstants.COMMCARE_BUNDLE_KEY] (for key-values result)
    // or [CommCareConstants.COMMCARE_DATA_KEY] (for a single integer or string), but apparently due to a bug in commcare
    // if we send [CommCareConstants.COMMCARE_BUNDLE_KEY] only, the result is processed correctly, but a toast shows an
    // error message. That is because commcare can't find [CommCareConstants.COMMCARE_DATA_KEY]
    private fun Bundle.toCommCareBundle(): Bundle = bundleOf(
        CommCareConstants.COMMCARE_DATA_KEY to "",
        CommCareConstants.COMMCARE_BUNDLE_KEY to this
    )
}

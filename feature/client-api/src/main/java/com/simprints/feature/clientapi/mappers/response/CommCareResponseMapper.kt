package com.simprints.feature.clientapi.mappers.response

import android.os.Bundle
import androidx.core.os.bundleOf
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.feature.clientapi.models.CommCareConstants
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.libsimprints.Constants
import javax.inject.Inject
import com.simprints.libsimprints.Identification as LegacyIdentification
import com.simprints.libsimprints.Tier as LegacyTier

internal class CommCareResponseMapper @Inject constructor(
    @param:DeviceID private val deviceId: String,
    @param:PackageVersionName private val appVersionName: String,
) {
    operator fun invoke(response: ActionResponse): Bundle = when (response) {
        is ActionResponse.EnrolActionResponse -> bundleOf(
            CommCareConstants.COMMCARE_DEVICE_ID to deviceId,
            CommCareConstants.COMMCARE_SID_VERSION to appVersionName,
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
            CommCareConstants.REGISTRATION_GUID_KEY to response.enrolledGuid,
        ).appendCoSyncData(response.subjectActions).toCommCareBundle()

        /**
         * CommCare expects Identification result as ParcelableArrayList containing Identification
         * objects in LibSimprints 1.0.12 format. That's why it is being returned in a different way
         * from others (not inside [CommCareConstants.COMMCARE_BUNDLE_KEY]).
         */
        is ActionResponse.IdentifyActionResponse -> bundleOf(
            CommCareConstants.COMMCARE_DEVICE_ID to deviceId,
            CommCareConstants.COMMCARE_SID_VERSION to appVersionName,
            Constants.SIMPRINTS_SESSION_ID to response.sessionId,
            Constants.SIMPRINTS_IDENTIFICATIONS to ArrayList<LegacyIdentification>(
                response.identifications.map {
                    LegacyIdentification(it.guid, it.confidenceScore, LegacyTier.valueOf(it.tier.name))
                },
            ),
        )

        is ActionResponse.ConfirmActionResponse -> bundleOf(
            CommCareConstants.COMMCARE_DEVICE_ID to deviceId,
            CommCareConstants.COMMCARE_SID_VERSION to appVersionName,
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
        ).toCommCareBundle()

        is ActionResponse.VerifyActionResponse -> bundleOf(
            CommCareConstants.COMMCARE_DEVICE_ID to deviceId,
            CommCareConstants.COMMCARE_SID_VERSION to appVersionName,
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
            CommCareConstants.VERIFICATION_GUID_KEY to response.matchResult.guid,
            CommCareConstants.VERIFICATION_CONFIDENCE_KEY to response.matchResult.confidenceScore.toString(),
            CommCareConstants.VERIFICATION_TIER_KEY to response.matchResult.tier.name,
        ).also {
            response.matchResult.verificationSuccess?.let { verificationSuccess ->
                it.putString(CommCareConstants.VERIFICATION_SUCCESS_KEY, verificationSuccess.toString())
            }
        }.toCommCareBundle()

        is ActionResponse.ExitFormActionResponse -> bundleOf(
            CommCareConstants.COMMCARE_DEVICE_ID to deviceId,
            CommCareConstants.COMMCARE_SID_VERSION to appVersionName,
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to "true",
            CommCareConstants.EXIT_REASON to response.reason,
            CommCareConstants.EXIT_EXTRA to response.extraText,
        ).toCommCareBundle()

        is ActionResponse.ErrorActionResponse -> bundleOf(
            CommCareConstants.COMMCARE_DEVICE_ID to deviceId,
            CommCareConstants.COMMCARE_SID_VERSION to appVersionName,
            CommCareConstants.SIMPRINTS_SESSION_ID to response.sessionId,
            CommCareConstants.BIOMETRICS_COMPLETE_CHECK_KEY to response.flowCompleted.toString(),
        ).toCommCareBundle()
    }

    private fun Bundle.appendCoSyncData(actions: String?) = apply {
        actions?.let { putString(Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS, it) }
    }

    // Based on the documentation, we are supposed to send either [CommCareConstants.COMMCARE_BUNDLE_KEY] (for key-values result)
    // or [CommCareConstants.COMMCARE_DATA_KEY] (for a single integer or string), but apparently due to a bug in commcare
    // if we send [CommCareConstants.COMMCARE_BUNDLE_KEY] only, the result is processed correctly, but a toast shows an
    // error message. That is because commcare can't find [CommCareConstants.COMMCARE_DATA_KEY]
    private fun Bundle.toCommCareBundle(): Bundle = bundleOf(
        CommCareConstants.COMMCARE_DATA_KEY to "",
        CommCareConstants.COMMCARE_BUNDLE_KEY to this,
    )
}

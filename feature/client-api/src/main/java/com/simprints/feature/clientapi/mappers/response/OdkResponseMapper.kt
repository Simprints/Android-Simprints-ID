package com.simprints.feature.clientapi.mappers.response

import android.os.Bundle
import androidx.core.os.bundleOf
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.models.ActionResponse
import com.simprints.feature.clientapi.models.OdkConstants
import com.simprints.moduleapi.app.responses.IAppMatchConfidence
import com.simprints.moduleapi.app.responses.IAppMatchResult
import javax.inject.Inject

internal class OdkResponseMapper @Inject constructor() {

    operator fun invoke(response: ActionResponse): Bundle = when (response) {
        is ActionResponse.EnrolActionResponse -> bundleOf(
            OdkConstants.ODK_REGISTRATION_ID_KEY to response.enrolledGuid,
            OdkConstants.ODK_SESSION_ID to response.sessionId,
        ).addFlowCompletedCheckBasedOnAction(response.request, true)

        is ActionResponse.IdentifyActionResponse -> bundleOf(
            OdkConstants.ODK_SESSION_ID to response.sessionId,
            OdkConstants.ODK_GUIDS_KEY to response.identifications.joinField { it.guid },
            OdkConstants.ODK_CONFIDENCES_KEY to response.identifications.joinField { it.confidenceScore.toString() },
            OdkConstants.ODK_TIERS_KEY to response.identifications.joinField { it.tier.name },
            OdkConstants.ODK_MATCH_CONFIDENCE_FLAGS_KEY to response.identifications.joinField { it.matchConfidence.name },
            OdkConstants.ODK_HIGHEST_MATCH_CONFIDENCE_FLAG_KEY to response.identifications.getHighestConfidence().name,
        ).addFlowCompletedCheckBasedOnAction(response.request, true)

        is ActionResponse.ConfirmActionResponse -> bundleOf(
            OdkConstants.ODK_SESSION_ID to response.sessionId,
        ).addFlowCompletedCheckBasedOnAction(response.request, true)

        is ActionResponse.VerifyActionResponse -> bundleOf(
            OdkConstants.ODK_SESSION_ID to response.sessionId,
            OdkConstants.ODK_GUIDS_KEY to response.matchResult.guid,
            OdkConstants.ODK_CONFIDENCES_KEY to response.matchResult.confidenceScore.toString(),
            OdkConstants.ODK_TIERS_KEY to response.matchResult.tier.name,
        ).addFlowCompletedCheckBasedOnAction(response.request, true)

        is ActionResponse.ExitFormActionResponse -> bundleOf(
            OdkConstants.ODK_SESSION_ID to response.sessionId,
            OdkConstants.ODK_EXIT_REASON to response.reason,
            OdkConstants.ODK_EXIT_EXTRA to response.extraText,
        ).addFlowCompletedCheckBasedOnAction(response.request, true)

        is ActionResponse.ErrorActionResponse -> bundleOf(
            OdkConstants.ODK_SESSION_ID to response.sessionId,
        ).addFlowCompletedCheckBasedOnAction(response.request, response.flowCompleted)
    }

    private fun Bundle.addFlowCompletedCheckBasedOnAction(action: ActionRequest, flowCompletedCheck: Boolean) = apply {
        when (action) {
            is ActionRequest.EnrolActionRequest -> putBoolean(OdkConstants.ODK_REGISTER_BIOMETRICS_COMPLETE, flowCompletedCheck)
            is ActionRequest.IdentifyActionRequest -> putBoolean(OdkConstants.ODK_IDENTIFY_BIOMETRICS_COMPLETE, flowCompletedCheck)
            is ActionRequest.ConfirmActionRequest -> putBoolean(OdkConstants.ODK_CONFIRM_IDENTITY_BIOMETRICS_COMPLETE, flowCompletedCheck)
            is ActionRequest.VerifyActionRequest -> putBoolean(OdkConstants.ODK_VERIFY_BIOMETRICS_COMPLETE, flowCompletedCheck)
            is ActionRequest.EnrolLastBiometricActionRequest -> putBoolean(OdkConstants.ODK_REGISTER_BIOMETRICS_COMPLETE, flowCompletedCheck)
        }
    }

    private fun List<IAppMatchResult>.joinField(block: (IAppMatchResult) -> String) = joinToString(separator = " ", transform = block).trim()

    private fun List<IAppMatchResult>.getHighestConfidence() = this.map { it.matchConfidence }.toSet().let {
        when {
            it.contains(IAppMatchConfidence.HIGH) -> IAppMatchConfidence.HIGH
            it.contains(IAppMatchConfidence.MEDIUM) -> IAppMatchConfidence.MEDIUM
            it.contains(IAppMatchConfidence.LOW) -> IAppMatchConfidence.LOW
            else -> IAppMatchConfidence.NONE
        }
    }
}

package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.simprints.feature.consent.ConsentContract
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.consent.screens.ConsentWrapperActivity
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.id.data.exitform.ExitFormReason.Companion.fromExitFormOption
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.*
import com.simprints.id.orchestrator.steps.core.requests.*
import com.simprints.id.orchestrator.steps.core.response.*
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import javax.inject.Inject

class CoreStepProcessorImpl @Inject constructor() : CoreStepProcessor {

    companion object {
        const val CONSENT_ACTIVITY_NAME = "com.simprints.feature.consent.screens.ConsentWrapperActivity"
        const val FETCH_GUID_ACTIVITY_NAME =
            "com.simprints.id.activities.fetchguid.FetchGuidActivity"
        const val GUID_SELECTION_ACTIVITY_NAME =
            "com.simprints.id.activities.guidselection.GuidSelectionActivity"
        const val LAST_BIOMETRICS_CORE_ACTIVITY_NAME =
            "com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity"
    }

    override fun buildStepConsent(consentType: ConsentType) =
        buildConsentStep(consentType)

    private fun buildConsentStep(consentType: ConsentType) = Step(
        requestCode = CONSENT.value,
        activityName = CONSENT_ACTIVITY_NAME,
        bundleKey = ConsentWrapperActivity.CONSENT_ARGS_EXTRA,
        request = ConsentContract.getArgs(consentType),
        status = Step.Status.NOT_STARTED
    )

    override fun buildFetchGuidStep(projectId: String, verifyGuid: String) = Step(
        requestCode = FETCH_GUID_CHECK.value,
        activityName = FETCH_GUID_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = FetchGUIDRequest(projectId, verifyGuid),
        status = Step.Status.NOT_STARTED
    )

    override fun buildConfirmIdentityStep(
        projectId: String,
        sessionId: String,
        selectedGuid: String
    ) = Step(
        requestCode = GUID_SELECTION_CODE.value,
        activityName = GUID_SELECTION_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = GuidSelectionRequest(projectId, sessionId, selectedGuid),
        status = Step.Status.NOT_STARTED
    )

    override fun buildAppEnrolLastBiometricsStep(
        projectId: String,
        userId: String,
        moduleId: String,
        previousSteps: List<Step>,
        sessionId: String?
    ) = Step(
        requestCode = LAST_BIOMETRICS_CORE.value,
        activityName = LAST_BIOMETRICS_CORE_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = EnrolLastBiometricsRequest(projectId, userId, moduleId, previousSteps, sessionId),
        status = Step.Status.NOT_STARTED
    )

    override fun processResult(data: Intent?): Step.Result? {
        val coreResponse = data?.getParcelableExtra<CoreResponse>(CORE_STEP_BUNDLE)

        return if (coreResponse != null) {
            when (coreResponse.type) {
                CoreResponseType.CONSENT -> null // Handled by else branch
                CoreResponseType.FETCH_GUID -> data.getParcelableExtra<FetchGUIDResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.EXIT_FORM -> data.getParcelableExtra<ExitFormResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.GUID_SELECTION -> data.getParcelableExtra<GuidSelectionResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.SETUP -> data.getParcelableExtra<SetupResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.ENROL_LAST_BIOMETRICS -> data.getParcelableExtra<EnrolLastBiometricsResponse>(CORE_STEP_BUNDLE)
            }
        } else {
            data?.extras?.let { handleFeatureModuleResponses(it) }
        }
    }

    private fun handleFeatureModuleResponses(data: Bundle): Step.Result? =
        if (data.containsKey(ConsentContract.CONSENT_RESULT)) {
            when (val result = data.getParcelable<Parcelable>(ConsentContract.CONSENT_RESULT)) {
                is ConsentResult -> AskConsentResponse(if (result.accepted) ConsentResponse.ACCEPTED else ConsentResponse.DECLINED)
                is ExitFormResult -> result.submittedOption()?.let { ExitFormResponse(fromExitFormOption(it), result.reason.orEmpty()) }
                else -> null
            }
        } else null
}

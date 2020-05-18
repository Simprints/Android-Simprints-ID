package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.*
import com.simprints.id.orchestrator.steps.core.requests.*
import com.simprints.id.orchestrator.steps.core.response.*
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE

class CoreStepProcessorImpl : CoreStepProcessor {

    companion object {
        const val CONSENT_ACTIVITY_NAME = "com.simprints.id.activities.consent.ConsentActivity"
        const val FETCH_GUID_ACTIVITY_NAME = "com.simprints.id.activities.fetchguid.FetchGuidActivity"
        const val GUID_SELECTION_ACTIVITY_NAME = "com.simprints.id.activities.guidselection.GuidSelectionActivity"
        const val LAST_BIOMETRICS_CORE_ACTIVITY_NAME = "com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity"
    }

    override fun buildStepConsent(consentType: ConsentType) =
        buildConsentStep(consentType)


    private fun buildConsentStep(consentType: ConsentType) = Step(
        requestCode = CONSENT.value,
        activityName = CONSENT_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = AskConsentRequest(consentType),
        status = Step.Status.NOT_STARTED
    )

    override fun buildFetchGuidStep(projectId: String, verifyGuid: String) = Step(
        requestCode = FETCH_GUID_CODE.value,
        activityName = FETCH_GUID_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = FetchGUIDRequest(projectId, verifyGuid),
        status = Step.Status.NOT_STARTED
    )

    override fun buildConfirmIdentityStep(projectId: String,
                                          sessionId: String,
                                          selectedGuid: String) = Step(
        requestCode = GUID_SELECTION_CODE.value,
        activityName = GUID_SELECTION_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = GuidSelectionRequest(projectId, sessionId, selectedGuid),
        status = Step.Status.NOT_STARTED
    )

    override fun buildAppEnrolLastBiometricsStep(projectId: String,
                                                 userId: String,
                                                 moduleId: String,
                                                 metadata: String,
                                                 sessionId: String?) = Step(
        requestCode = LAST_BIOMETRICS_CORE.value,
        activityName = LAST_BIOMETRICS_CORE_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = EnrolLastBiometricsRequest(projectId, userId, moduleId, metadata, sessionId),
        status = Step.Status.NOT_STARTED
    )

    override fun processResult(data: Intent?): Step.Result? =
        data?.getParcelableExtra<CoreResponse>(CORE_STEP_BUNDLE)?.also { coreResponse ->
            when (coreResponse.type) {
                CoreResponseType.CONSENT -> data.getParcelableExtra<AskConsentResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.FINGERPRINT_EXIT_FORM -> data.getParcelableExtra<CoreFingerprintExitFormResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.FACE_EXIT_FORM -> data.getParcelableExtra<CoreFaceExitFormResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.FETCH_GUID -> data.getParcelableExtra<FetchGUIDResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.CORE_EXIT_FORM -> data.getParcelableExtra<CoreExitFormResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.GUID_SELECTION -> data.getParcelableExtra<GuidSelectionResponse>(CORE_STEP_BUNDLE)
            }
        }
}

package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.requests.FetchGUIDRequest
import com.simprints.id.domain.moduleapi.core.requests.SetupRequest
import com.simprints.id.domain.moduleapi.core.response.*
import com.simprints.id.domain.moduleapi.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.*

class CoreStepProcessorImpl : CoreStepProcessor {

    companion object {
        const val CONSENT_ACTIVITY_NAME = "com.simprints.id.activities.consent.ConsentActivity"
        const val VERIFY_ACTIVITY_NAME = "com.simprints.id.activities.fetchguid.FetchGuidActivity"
        const val SETUP_ACTIVITY_NAME = ""
    }

    override fun buildStepSetup(): Step = buildSetupStep()

    override fun buildStepConsent(consentType: ConsentType) =
        buildConsentStep(consentType)

    override fun buildStepVerify(projectId: String, verifyGuid: String): Step =
        buildVerifyStep(projectId, verifyGuid)

    private fun buildSetupStep() = Step(
        requestCode = SETUP.value,
        activityName = SETUP_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = SetupRequest(),
        status = Step.Status.NOT_STARTED
    )

    private fun buildConsentStep(consentType: ConsentType) = Step(
        requestCode = CONSENT.value,
        activityName = CONSENT_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = AskConsentRequest(consentType),
        status = Step.Status.NOT_STARTED
    )

    private fun buildVerifyStep(projectId: String, verifyGuid: String) = Step(
        requestCode = VERIFICATION_CHECK.value,
        activityName = VERIFY_ACTIVITY_NAME,
        bundleKey = CORE_STEP_BUNDLE,
        request = FetchGUIDRequest(projectId, verifyGuid),
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
            }
        }
}

package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest.Companion.CONSENT_STEP_BUNDLE
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.requests.FetchGUIDRequest
import com.simprints.id.domain.moduleapi.core.response.AskConsentResponse
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.FingerprintExitFormResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.CONSENT
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.VERIFICATION_CHECK

class CoreStepProcessorImpl : CoreStepProcessor {

    companion object {
        const val CORE_ACTIVITY_NAME = "com.simprints.id.activities.consent.ConsentActivity"
    }

    override fun buildStepConsent(consentType: ConsentType) =
        buildConsentStep(consentType)

    //Building normal ConsentStep for now
    override fun buildStepVerify(): Step =
        buildVerifyStep()

    private fun buildConsentStep(consentType: ConsentType) =
        Step(CONSENT.value, CORE_ACTIVITY_NAME, CONSENT_STEP_BUNDLE,
            AskConsentRequest(consentType), status = Step.Status.NOT_STARTED)

    //STOPSHIP: Will be done in the story for adding verification step. Building
    private fun buildVerifyStep() =
        Step(VERIFICATION_CHECK.value, CORE_ACTIVITY_NAME, CONSENT_STEP_BUNDLE,
            FetchGUIDRequest(), status = Step.Status.NOT_STARTED)

    override fun processResult(resultCode: Int, data: Intent?): Step.Result? =
        when (resultCode) {
            CoreResponseCode.CONSENT.value -> data?.getParcelableExtra<AskConsentResponse>(CONSENT_STEP_BUNDLE)
            CoreResponseCode.CORE_EXIT_FORM.value -> data?.getParcelableExtra<CoreExitFormResponse>(CONSENT_STEP_BUNDLE)
            CoreResponseCode.FINGERPRINT_EXIT_FORM.value -> data?.getParcelableExtra<FingerprintExitFormResponse>(CONSENT_STEP_BUNDLE)
            CoreResponseCode.FACE_EXIT_FORM.value -> TODO("Will be implemented with face exit form")
            CoreResponseCode.FETCH_GUID.value -> TODO("Will be implemented with verification check")
            CoreResponseCode.ERROR.value -> TODO("Will be implemented with verification check")
            else -> throw IllegalStateException("Invalid result code from core step processor")
        }
}

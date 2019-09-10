package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest.Companion.CONSENT_STEP_BUNDLE
import com.simprints.id.domain.moduleapi.core.response.AskConsentResponse
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.requests.FetchGUIDRequest
import com.simprints.id.orchestrator.steps.Step

class CoreStepProcessorImpl: CoreStepProcessor {

    companion object {
        const val CORE_ACTIVITY_NAME = "com.simprints.id.activities.consent.ConsentActivity"
    }
    override fun buildStepConsent(consentType: ConsentType)=
        buildConsentStep(consentType)

    //Building normal ConsentStep for now
    override fun buildStepVerify(): Step =
        buildVerifyStep()

    private fun buildConsentStep(consentType: ConsentType) =
        Step(CoreRequestCode.CONSENT.value, CORE_ACTIVITY_NAME, CONSENT_STEP_BUNDLE,
            AskConsentRequest(consentType), Step.Status.NOT_STARTED)

    //STOPSHIP: Will be done in the story for adding verification step. Building
    private fun buildVerifyStep() =
        Step(CoreRequestCode.VERIFICATION_CHECK.value, CORE_ACTIVITY_NAME, CONSENT_STEP_BUNDLE,
            FetchGUIDRequest(), Step.Status.NOT_STARTED)

    override fun processResult(resultCode: Int, data: Intent?): Step.Result? =
        data?.getParcelableExtra<AskConsentResponse>(CONSENT_STEP_BUNDLE)
}

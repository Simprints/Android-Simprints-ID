package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.requests.FetchGUIDRequest
import com.simprints.id.domain.moduleapi.core.response.AskConsentResponse
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.domain.moduleapi.core.response.CoreResponseType
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
        Step(CONSENT.value, CORE_ACTIVITY_NAME, CORE_STEP_BUNDLE,
            AskConsentRequest(consentType), status = Step.Status.NOT_STARTED)

    //STOPSHIP: Will be done in the story for adding verification step. Building
    private fun buildVerifyStep() =
        Step(VERIFICATION_CHECK.value, CORE_ACTIVITY_NAME, CORE_STEP_BUNDLE,
            FetchGUIDRequest(), status = Step.Status.NOT_STARTED)

    override fun processResult(data: Intent?): Step.Result? =
        data?.getParcelableExtra<CoreResponse>(CORE_STEP_BUNDLE)?.also { coreResponse ->
            when (coreResponse.type) {
                CoreResponseType.CONSENT -> data.getParcelableExtra<AskConsentResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.EXIT_FORM -> data.getParcelableExtra<CoreExitFormResponse>(CORE_STEP_BUNDLE)
                CoreResponseType.FETCH_GUID -> TODO("Will be implemented with verification check")
            }
        }
}

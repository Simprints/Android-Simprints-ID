package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step
import kotlinx.android.parcel.Parcelize

class CoreStepProcessorImpl: CoreStepProcessor {

    companion object {
        private const val ACTIVITY_NAME = "com.simprints.id.activities.consent.ConsentActivity"
        private const val BUNDLE_NAME = "core_step_bundle"
    }
    override fun buildStepEnrolOrIdentify(): List<Step> =
        listOf(buildConsentStep())


    override fun buildStepVerify(): List<Step> =
        listOf(buildVerifyStep(), buildConsentStep())


    private fun buildConsentStep() = Step(CoreRequestCode.CONSENT.value, ACTIVITY_NAME, BUNDLE_NAME,
        CoreStepRequest("", "", "", ""), Step.Status.NOT_STARTED)

    private fun buildVerifyStep() = Step(CoreRequestCode.VERIFICATION_CHECK.value, ACTIVITY_NAME, BUNDLE_NAME,
        CoreStepRequest("", "", "",""), Step.Status.NOT_STARTED)

    override fun processResult(resultCode: Int, data: Intent?): Step.Result? =
        data?.getParcelableExtra<CoreStepResponse>(BUNDLE_NAME)
}

@Parcelize
data class CoreStepRequest(override val projectId: String,
                           override val moduleId: String,
                           override val userId: String,
                           override val metadata: String) : Step.Request, AppRequest

@Parcelize
data class CoreStepResponse(val projectId: String): Step.Result

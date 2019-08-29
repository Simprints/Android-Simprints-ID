package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.responses.AppRefusalFormResponse
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
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
        CoreStepRequest("", "", ""), Step.Status.NOT_STARTED)

    private fun buildVerifyStep() = Step(CoreRequestCode.VERIFICATION_CHECK.value, ACTIVITY_NAME, BUNDLE_NAME,
        CoreStepRequest("", "", ""), Step.Status.NOT_STARTED)

    override fun processResult(resultCode: Int, data: Intent?): Step.Result? =
        data?.getParcelableExtra<AppResponse>(BUNDLE_NAME)?.let {
            when(it) {
                is AppRefusalFormResponse -> it
                else -> null
            }
        }
}

@Parcelize
data class CoreStepRequest(val projectId: String,
                           val moduleId: String,
                           val userId: String) : Step.Request

package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.domain.moduleapi.core.CoreStepRequest
import com.simprints.id.domain.moduleapi.core.CoreStepRequest.Companion.CORE_STEP_BUNDLE
import com.simprints.id.domain.moduleapi.core.CoreStepResponse
import com.simprints.id.orchestrator.steps.Step

class CoreStepProcessorImpl: CoreStepProcessor {

    companion object {
        const val CORE_ACTIVITY_NAME = "com.simprints.id.activities.consent.ConsentActivity"
    }
    override fun buildStepConsent(projectId: String, userId: String,
                                  moduleId: String, metadata: String)=
        buildConsentStep(projectId, userId, moduleId, metadata)

    //Building normal ConsentStep for now
    override fun buildStepVerify(projectId: String, userId: String,
                                 moduleId: String, metadata: String): Step =
        buildVerifyStep(projectId, userId, moduleId, metadata)

    private fun buildConsentStep(projectId: String, userId: String, moduleId: String, metadata: String) =
        Step(CoreRequestCode.CONSENT.value, CORE_ACTIVITY_NAME, CORE_STEP_BUNDLE,
        CoreStepRequest(projectId = projectId, userId = userId,
            moduleId = moduleId, metadata = metadata), Step.Status.NOT_STARTED)

    //STOPSHIP: Will be done in the story for adding verification step. Building
    private fun buildVerifyStep(projectId: String, userId: String, moduleId: String, metadata: String) =
        Step(CoreRequestCode.VERIFICATION_CHECK.value, CORE_ACTIVITY_NAME, CORE_STEP_BUNDLE,
        CoreStepRequest(projectId = projectId, userId = userId,
            moduleId = moduleId, metadata = metadata), Step.Status.NOT_STARTED)

    override fun processResult(resultCode: Int, data: Intent?): Step.Result? =
        data?.getParcelableExtra<CoreStepResponse>(CORE_STEP_BUNDLE)
}

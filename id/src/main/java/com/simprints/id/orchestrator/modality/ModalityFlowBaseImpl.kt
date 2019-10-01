package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequestType
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFaceExitFormResponse
import com.simprints.id.domain.moduleapi.core.response.CoreFingerprintExitFormResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor

abstract class ModalityFlowBaseImpl(private val coreStepProcessor: CoreStepProcessor): ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        when (appRequest.type) {
            AppRequestType.ENROL -> steps.add(buildConsentStep(ConsentType.ENROL))
            AppRequestType.IDENTIFY -> steps.add(buildConsentStep(ConsentType.IDENTIFY))
            AppRequestType.VERIFY -> {
                addVerifyStep(appRequest)
                steps.add(buildConsentStep(ConsentType.VERIFY))
            }
        }
    }

    private fun addVerifyStep(appRequest: AppRequest) {
        with(appRequest as AppVerifyRequest) {
            steps.add(buildVerifyCoreStep(projectId, verifyGuid))
        }
    }

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }

    private fun buildConsentStep(consentType: ConsentType) =
        coreStepProcessor.buildStepConsent(consentType)

    private fun buildVerifyCoreStep(projectId: String, verifyGuid: String) =
        coreStepProcessor.buildStepVerify(projectId, verifyGuid)

    fun completeAllStepsIfExitFormHappened(data: Intent?) =
        coreStepProcessor.processResult(data).also { coreResult ->
            if (isExitFormResponse(coreResult)) {
                steps.forEach { it.setStatus(Step.Status.COMPLETED)  }
            }
        }

    private fun isExitFormResponse(coreResult: Step.Result?) =
        coreResult is CoreExitFormResponse ||
            coreResult is CoreFingerprintExitFormResponse ||
            coreResult is CoreFaceExitFormResponse
}

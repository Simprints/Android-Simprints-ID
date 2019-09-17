package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequestType
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.response.CoreExitFormResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor

abstract class ModalityFlowBaseImpl(private val coreStepProcessor: CoreStepProcessor): ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        when (appRequest.type) {
            AppRequestType.ENROL -> steps.add(buildCoreStep(ConsentType.ENROL))
            AppRequestType.IDENTIFY -> steps.add(buildCoreStep(ConsentType.IDENTIFY))
            AppRequestType.VERIFY -> steps.add(buildVerifyCoreStep())
        }
    }

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }

    private fun buildCoreStep(consentType: ConsentType) =
        coreStepProcessor.buildStepConsent(consentType)


    private fun buildVerifyCoreStep() =
        coreStepProcessor.buildStepVerify()

    fun processResult(data: Intent?) =
        coreStepProcessor.processResult(data).also { coreResult ->
            if (coreResult is CoreExitFormResponse) {
                steps.forEach { it.setStatus(Step.Status.COMPLETED)  }
            }
        }
}

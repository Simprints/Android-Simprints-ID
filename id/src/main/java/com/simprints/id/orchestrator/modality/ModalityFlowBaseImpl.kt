package com.simprints.id.orchestrator.modality

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor

abstract class ModalityFlowBaseImpl(private val coreStepProcessor: CoreStepProcessor): ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        appRequest.let {
            when (it) {
                is AppEnrolRequest -> {
                    steps.add(buildCoreStep(ConsentType.ENROL))
                }
                is AppIdentifyRequest -> {
                    steps.add(buildCoreStep(ConsentType.IDENTIFY))
                }
                is AppVerifyRequest -> {
                    steps.add(buildVerifyCoreStep())
                }
                else -> Throwable("invalid AppRequest")
            }
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
}

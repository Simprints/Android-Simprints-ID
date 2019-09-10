package com.simprints.id.orchestrator.modality

import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor

abstract class ModalityFlowBaseImpl(private val coreStepProcessor: CoreStepProcessor): ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest, modalities: List<Modality>) {
        appRequest.let {
            when (it) {
                is AppEnrolRequest, is AppIdentifyRequest -> {
                    steps.add(buildCoreStep(it.projectId, it.userId, it.moduleId, it.metadata))
                }
                is AppVerifyRequest -> {
                    steps.add(buildVerifyCoreStep(it.projectId, it.userId, it.moduleId, it.metadata))
                }
                else -> Throwable("invalid AppRequest")
            }
        }
    }

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }

    private fun buildCoreStep(projectId: String, userId: String, moduleId: String, metadata: String) =
        coreStepProcessor.buildStepEnrolOrIdentify(projectId, userId, moduleId, metadata)


    private fun buildVerifyCoreStep(projectId: String, userId: String, moduleId: String, metadata: String) =
        coreStepProcessor.buildStepVerify(projectId, userId, moduleId, metadata)
}

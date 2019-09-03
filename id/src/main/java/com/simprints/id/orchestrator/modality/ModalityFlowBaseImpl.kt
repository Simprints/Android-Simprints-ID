package com.simprints.id.orchestrator.modality

import android.content.Intent
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
        with(appRequest) {
            when {
                this is AppEnrolRequest || this is AppIdentifyRequest -> {
                    steps.add(buildCoreStep(projectId, userId, moduleId, metadata))
                }
                this is AppVerifyRequest -> {
                    steps.add(buildVerifyCoreStep(projectId, userId, moduleId, metadata))
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

    fun processResult(requestCode: Int, data: Intent?) = coreStepProcessor.processResult(requestCode, data)
}

package com.simprints.id.orchestrator.modality

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.core.CoreRequestCode.Companion.isCoreResult
import com.simprints.id.orchestrator.steps.core.CoreStepProcessor

class ModalityFlowEnrolLastBiometrics(private val coreStepProcessor: CoreStepProcessor,
                                      private val hotCache: HotCache) : ModalityFlow {

    override val steps: MutableList<Step> = mutableListOf()

    override fun startFlow(appRequest: AppRequest) {
        require(appRequest is AppEnrolLastBiometricsRequest)
        steps.addAll(buildStepsList(appRequest, hotCache.load()))
    }

    private fun buildStepsList(appRequest: AppEnrolLastBiometricsRequest,
                               previousSteps: List<Step>) =
        listOf(coreStepProcessor.buildAppEnrolLastBiometricsStep(
            appRequest.projectId,
            appRequest.userId,
            appRequest.moduleId,
            previousSteps,
            appRequest.identificationSessionId
        ))

    override fun restoreState(stepsToRestore: List<Step>) {
        steps.clear()
        steps.addAll(stepsToRestore)
    }

    override fun getNextStepToLaunch(): Step? = steps.firstOrNull { it.getStatus() == Step.Status.NOT_STARTED }

    override suspend fun handleIntentResult(appRequest: AppRequest,
                                            requestCode: Int,
                                            resultCode: Int, data: Intent?): Step? {
        val result = when {
            isCoreResult(requestCode) -> coreStepProcessor.processResult(data)
            else -> throw IllegalStateException("Invalid result from intent")
        }

        val stepForRequest = steps.firstOrNull { it.requestCode == requestCode }
        return stepForRequest?.apply { setResult(result) }
    }
}

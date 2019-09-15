package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING

open class OrchestratorManagerImpl(
    private val flowModalityFactory: ModalityFlowFactory,
    private val appResponseFactory: AppResponseFactory,
    private val hotCache: HotCache
) : OrchestratorManager {

    override val ongoingStep = MutableLiveData<Step?>()
    override val appResponse = MutableLiveData<AppResponse?>()

    internal lateinit var modalities: List<Modality>
    internal lateinit var appRequest: AppRequest
    internal var sessionId: String = ""

    private lateinit var modalitiesFlow: ModalityFlow

    override fun initialise(modalities: List<Modality>,
                            appRequest: AppRequest,
                            sessionId: String) {
        this.sessionId = sessionId
        this.appRequest = appRequest
        this.modalities = modalities
        modalitiesFlow = flowModalityFactory.createModalityFlow(appRequest, modalities)
        resetInternalState()

        proceedToNextStepOrAppResponse()
    }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResult(requestCode, resultCode, data)?.let(hotCache::save)
        proceedToNextStepOrAppResponse()
    }

    override fun restoreState() {
        resetInternalState()
        hotCache.load()?.let(modalitiesFlow::restoreState)
        proceedToNextStepOrAppResponse()
    }

    override fun clearState() {
        hotCache.clear()
    }

    private fun proceedToNextStepOrAppResponse() {
        with(modalitiesFlow) {
            if (!anyStepOngoing()) {
                val potentialNextStep = getNextStepToLaunch()
                if (potentialNextStep != null) {
                    startStep(potentialNextStep)
                } else {
                    buildAppResponse()
                }
            }
        }
    }

    private fun startStep(step: Step) {
        step.setStatus(ONGOING)
        ongoingStep.value = step
        appResponse.value = null
        hotCache.save(step)
    }

    private fun ModalityFlow.anyStepOngoing() =
        steps.any { it.getStatus() == ONGOING }

    private fun buildAppResponse() {
        val cachedSteps = hotCache.load()
        val steps: List<Step> = if (cachedSteps.isNullOrEmpty()) {
            modalitiesFlow.steps
        } else {
            cachedSteps
        }
        val appResponseToReturn = appResponseFactory.buildAppResponse(
            modalities, appRequest, steps, sessionId
        )
        ongoingStep.value = null
        appResponse.value = appResponseToReturn
    }

    private fun resetInternalState() {
        appResponse.value = null
        ongoingStep.value = null
    }

}

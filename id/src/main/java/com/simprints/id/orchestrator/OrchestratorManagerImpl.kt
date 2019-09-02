package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING

open class OrchestratorManagerImpl(private val flowModalityFactory: ModalityFlowFactory,
                                   private val appResponseFactory: AppResponseFactory) : OrchestratorManager {

    override val onGoingStep = MutableLiveData<Step?>()
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
        modalitiesFlow.handleIntentResult(requestCode, resultCode, data)
        proceedToNextStepOrAppResponse()
    }

    override fun restoreState(steps: List<Step>) {
        resetInternalState()

        modalitiesFlow.restoreState(steps)
        proceedToNextStepOrAppResponse()
    }

    override fun getState(): List<Step> = modalitiesFlow.steps

    private fun proceedToNextStepOrAppResponse() {
        with(modalitiesFlow) {
            if (!anyStepOnGoing()) {
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
        step.status = ONGOING
        onGoingStep.value = step
        appResponse.value = null
    }

    private fun ModalityFlow.anyStepOnGoing() =
        steps.any { it.status == ONGOING }

    private fun buildAppResponse() {
        val appResponseToReturn = appResponseFactory.buildAppResponse(modalities, appRequest, modalitiesFlow.steps, sessionId)
        onGoingStep.value = null
        appResponse.value = appResponseToReturn
    }

    private fun resetInternalState() {
        appResponse.value = null
        onGoingStep.value = null
    }
}

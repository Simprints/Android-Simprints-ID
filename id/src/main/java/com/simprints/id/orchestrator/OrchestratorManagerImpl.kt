package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.builders.AppResponseFactory
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING

open class OrchestratorManagerImpl(private val flowModalityFactory: ModalityFlowFactory,
                                   private val appResponseFactory: AppResponseFactory) : OrchestratorManager {

    override val onGoingStep = MutableLiveData<Step>()
    override val appResponse = MutableLiveData<AppResponse>()

    internal lateinit var modalities: List<Modality>
    internal lateinit var appRequest: AppRequest
    internal var sessionId: String = ""

    private val modalitiesFlow by lazy {
        flowModalityFactory.startModalityFlow(appRequest, modalities)
    }

    override suspend fun start(modalities: List<Modality>,
                               appRequest: AppRequest,
                               sessionId: String) {
        this.sessionId = sessionId
        this.appRequest = appRequest
        this.modalities = modalities

        proceedToNextIntentOrAppResponse()
    }

    override suspend fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResult(requestCode, resultCode, data)
        proceedToNextIntentOrAppResponse()
    }

    private fun proceedToNextIntentOrAppResponse() {
        with(modalitiesFlow) {
            if (!anyStepOnGoing()) {
                getNextStepToStart()?.let {
                    startStep(it)
                } ?: buildAppResponse()
            }
        }
    }

    private fun startStep(it: Step) {
        it.status = ONGOING
        onGoingStep.postValue(it)
    }

    private fun ModalityFlow.anyStepOnGoing() =
        steps.firstOrNull { it.status == ONGOING } != null

    private fun buildAppResponse() {
        val appResponseToReturn = appResponseFactory.buildAppResponse(modalities, appRequest, modalitiesFlow.steps, sessionId)
        appResponse.postValue(appResponseToReturn)
    }
}

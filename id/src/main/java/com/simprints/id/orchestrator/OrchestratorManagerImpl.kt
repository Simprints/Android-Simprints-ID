package com.simprints.id.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.builders.AppResponseFactory
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Request

open class OrchestratorManagerImpl(private val modality: Modality,
                                   private val flowModalityFactory: ModalityFlowFactory,
                                   private val appResponseFactory: AppResponseFactory) : OrchestratorManager {

    override val nextIntent = MutableLiveData<Request>()
    override val appResponse = MutableLiveData<AppResponse>()

    internal lateinit var appRequest: AppRequest
    internal var sessionId: String = ""

    internal val modalitiesFlow by lazy {
        flowModalityFactory.buildModalityFlow(appRequest, modality)
    }

    override suspend fun initOrchestrator(appRequest: AppRequest, sessionId: String) {
        this.sessionId = sessionId
        this.appRequest = appRequest
        proceedToNextIntentOrAppResponse()
    }

    override suspend fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResult(requestCode, resultCode, data)
        proceedToNextIntentOrAppResponse()
    }

    private fun proceedToNextIntentOrAppResponse() {
        modalitiesFlow.getLatestOngoingStep()?.request?.let {
            nextIntent.postValue(it)
        } ?: appResponse.postValue(buildAppResponse())
    }

    private fun buildAppResponse(): AppResponse {
        val results = modalitiesFlow.steps.mapNotNull { it.result }
        return appResponseFactory.buildAppResponse(modality, appRequest, results, sessionId)
    }

}

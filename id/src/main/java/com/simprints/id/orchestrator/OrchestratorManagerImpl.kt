package com.simprints.id.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.builders.AppResponseFactory
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.Request

open class OrchestratorManagerImpl(private val modality: Modality,
                                   private val flowModalityFactory: ModalityFlowFactory,
                                   private val appResponseFactory: AppResponseFactory) : OrchestratorManager {

    internal lateinit var appRequest: AppRequest
    private var sessionId: String = ""

    internal val modalitiesFlow by lazy {
        flowModalityFactory.buildModalityFlow(appRequest, modality)
    }

    override suspend fun initOrchestrator(appRequest: AppRequest, sessionId: String) {
        this.sessionId = sessionId
        this.appRequest = appRequest
    }

    override suspend fun getNextIntent(): Request? = modalitiesFlow.getLatestOngoingStep()?.request

    @SuppressLint("CheckResult")
    override suspend fun getAppResponse(): AppResponse? =
        try {
            val results = modalitiesFlow.steps.map { it.result }.filterNotNull()
            buildAndEmitFinalResult(results)
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }

    override suspend fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResult(requestCode, resultCode, data)
    }

    private fun buildAndEmitFinalResult(stepsResults: List<ModalityFlow.Result>) =
        appResponseFactory.buildAppResponse(modality, appRequest, stepsResults, sessionId)
}

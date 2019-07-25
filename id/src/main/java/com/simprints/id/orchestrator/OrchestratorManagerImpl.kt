package com.simprints.id.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.builders.AppResponseFactory
import io.reactivex.Observable
import io.reactivex.Single

open class OrchestratorManagerImpl(private val modality: Modality,
                                   private val flowModalityFactory: ModalityFlowFactory,
                                   private val appResponseFactory: AppResponseFactory) : OrchestratorManager {

    internal lateinit var appRequest: AppRequest
    private var sessionId: String = ""

    internal val modalitiesFlow by lazy {
        flowModalityFactory.buildModalityFlow(appRequest, modality)
    }

    @SuppressLint("CheckResult")
    override fun startFlow(appRequest: AppRequest,
                           sessionId: String): Observable<ModalityStepRequest> {

        this.sessionId = sessionId
        this.appRequest = appRequest

        return modalitiesFlow.modalityStepRequests
    }

    @SuppressLint("CheckResult")
    override fun getAppResponse(): Single<AppResponse> =
        modalitiesFlow.modalityResponses
            .toList()
            .map {
                buildAndEmitFinalResult(it)
            }

    internal fun buildAndEmitFinalResult(stepsResults: List<ModalityResponse>) =
        appResponseFactory.buildAppResponse(modality, appRequest, stepsResults, sessionId)

    @SuppressLint("CheckResult")
    override fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResponse(requestCode, resultCode, data)
    }
}

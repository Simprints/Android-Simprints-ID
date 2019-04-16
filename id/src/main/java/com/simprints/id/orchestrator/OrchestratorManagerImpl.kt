package com.simprints.id.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.exceptions.unexpected.UnexpectedErrorInModalFlow
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.builders.AppResponseFactory
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

open class OrchestratorManagerImpl(private val modality: Modality,
                                   private val flowModalityFactory: ModalityFlowFactory,
                                   private val appResponseFactory: AppResponseFactory) : OrchestratorManager {


    private var appResponseEmitter: SingleEmitter<AppResponse>? = null

    internal lateinit var appRequest: AppRequest
    internal val stepsResults: MutableList<ModalityResponse> = mutableListOf()
    private var sessionId: String = ""

    internal val modalitiesFlow by lazy {
        flowModalityFactory.buildModalityFlow(appRequest, modality)
    }

    @SuppressLint("CheckResult")
    override fun startFlow(appRequest: AppRequest,
                           sessionId: String): Observable<ModalityStepRequest> {

        this.sessionId = sessionId
        this.appRequest = appRequest
        stepsResults.clear()

        subscribeForStepsIntentResults(appRequest, sessionId)

        return modalitiesFlow.modalityStepRequests
    }

    @SuppressLint("CheckResult")
    internal fun subscribeForStepsIntentResults(appRequest: AppRequest, sessionId: String) {
        modalitiesFlow.modalityResponses
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    addNewStepIntentResult(it)
                },
                onComplete = {
                    buildAndEmitFinalResult(appRequest, sessionId)
                },
                onError = {
                    it.printStackTrace()
                    emitErrorAsFinalResult()
                })
    }

    internal fun emitErrorAsFinalResult() {
        appResponseEmitter?.onError(UnexpectedErrorInModalFlow())
    }

    internal fun buildAndEmitFinalResult(appRequest: AppRequest, sessionId: String) {
        appResponseEmitter?.onSuccess(appResponseFactory.buildAppResponse(modality, appRequest, stepsResults, sessionId))
    }

    internal fun addNewStepIntentResult(it: ModalityResponse) {
        stepsResults.add(it)
    }


    override fun getAppResponse(): Single<AppResponse> = Single.create {
        appResponseEmitter = it
    }

    @SuppressLint("CheckResult")
    override fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) {
        modalitiesFlow.handleIntentResponse(requestCode, resultCode, data)
    }
}

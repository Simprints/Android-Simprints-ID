package com.simprints.id.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.*
import com.simprints.id.domain.modality.ModalityResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.exceptions.unexpected.UnexpectedErrorInModalFlow
import com.simprints.id.orchestrator.modality.ModalityFlowFactory
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.orchestrator.modality.builders.AppResponseBuilderFactory
import com.simprints.id.orchestrator.modality.flows.FaceModalityFlow
import com.simprints.id.orchestrator.modality.flows.FingerprintModalityFlow
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

open class OrchestratorManagerImpl(val modality: Modality,
                                   flowModalityBuilder: ModalityFlowFactory,
                                   prefs: PreferencesManager,
                                   appResponseFactory: AppResponseBuilderFactory) : OrchestratorManager {

    companion object {
        private const val packageName = "com.simprints.id"
    }

    private var appResponseEmitter: SingleEmitter<AppResponse>? = null

    internal lateinit var appRequest: AppRequest
    internal val stepsResults: MutableList<ModalityResponse> = mutableListOf()
    private var sessionId: String = ""

    private val faceFlow by lazy { FaceModalityFlow(appRequest, packageName) }
    private val fingerprintFlow by lazy { FingerprintModalityFlow(appRequest, packageName, prefs) }

    internal val flowModality by lazy {
        when (modality) {
            FACE -> flowModalityBuilder.buildModalityFlow(arrayListOf(faceFlow))
            FINGER -> flowModalityBuilder.buildModalityFlow(arrayListOf(fingerprintFlow))
            FINGER_FACE -> flowModalityBuilder.buildModalityFlow(arrayListOf(fingerprintFlow, faceFlow))
            FACE_FINGER -> flowModalityBuilder.buildModalityFlow(arrayListOf(faceFlow, fingerprintFlow))
        }
    }

    private val appResponseBuilder = appResponseFactory.buildAppResponseBuilder(modality)

    @SuppressLint("CheckResult")
    override fun startFlow(appRequest: AppRequest,
                           sessionId: String): Observable<ModalityStepRequest> {

        this.sessionId = sessionId
        this.appRequest = appRequest
        stepsResults.clear()
        subscribeForStepsIntentResults(appRequest, sessionId)

        return flowModality.nextModalityStepRequest
    }

    @SuppressLint("CheckResult")
    internal fun subscribeForStepsIntentResults(appRequest: AppRequest, sessionId: String) {
        flowModality.modalityResponses
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
        appResponseEmitter?.onSuccess(appResponseBuilder.buildResponse(appRequest, stepsResults, sessionId))
    }

    internal fun addNewStepIntentResult(it: ModalityResponse) {
        stepsResults.add(it)
    }


    override fun getAppResponse(): Single<AppResponse> = Single.create {
        appResponseEmitter = it
    }

    @SuppressLint("CheckResult")
    override fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) {
        flowModality.handleIntentResponse(requestCode, resultCode, data)
    }
}

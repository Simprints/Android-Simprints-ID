package com.simprints.id.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modal.Modal
import com.simprints.id.domain.modal.Modal.*
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modals.ModalFlowBuilder
import com.simprints.id.orchestrator.modals.ModalStepRequest
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFace
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFaceFinger
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFinger
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFingerFace
import com.simprints.id.orchestrator.modals.flows.FaceModalFlow
import com.simprints.id.orchestrator.modals.flows.FingerprintModalFlow
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

open class OrchestratorManagerImpl(val modal: Modal,
                                   flowModalBuilder: ModalFlowBuilder,
                                   prefs: PreferencesManager) : OrchestratorManager {

    companion object {
        private const val packageName = "com.simprints.id"
    }

    private var appResponseEmitter: SingleEmitter<AppResponse>? = null

    internal lateinit var appRequest: AppRequest
    private val stepsResults: MutableList<ModalResponse> = mutableListOf()
    private var sessionId: String = ""

    private val faceFlow by lazy { FaceModalFlow(appRequest, packageName) }
    private val fingerprintFlow by lazy { FingerprintModalFlow(appRequest, packageName, prefs) }

    internal val flowModal by lazy {
        when (modal) {
            FACE -> flowModalBuilder.buildModalFlow(arrayListOf(faceFlow))
            FINGER -> flowModalBuilder.buildModalFlow(arrayListOf(fingerprintFlow))
            FINGER_FACE -> flowModalBuilder.buildModalFlow(arrayListOf(fingerprintFlow, faceFlow))
            FACE_FINGER -> flowModalBuilder.buildModalFlow(arrayListOf(faceFlow, fingerprintFlow))
        }
    }

    private val appResponseBuilder by lazy {
        when (modal) {
            FACE -> AppResponseBuilderForFace()
            FINGER -> AppResponseBuilderForFinger()
            FINGER_FACE -> AppResponseBuilderForFingerFace()
            FACE_FINGER -> AppResponseBuilderForFaceFinger()
        }
    }


    @SuppressLint("CheckResult")
    override fun startFlow(appRequest: AppRequest, sessionId: String): Observable<ModalStepRequest> {
        this.sessionId = sessionId
        this.appRequest = appRequest
        stepsResults.clear()
        subscribeForStepsIntentResults(appRequest, sessionId)

        return flowModal.nextIntent
    }

    @SuppressLint("CheckResult")
    private fun subscribeForStepsIntentResults(appRequest: AppRequest, sessionId: String) {
        flowModal.modalResponses
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = {
                        addNewStepIntentResult(it)
                    },
                    onComplete = {
                        buildAndEmitFinalResult(appRequest, sessionId)
                    })
    }

    private fun buildAndEmitFinalResult(appRequest: AppRequest, sessionId: String) {
        appResponseEmitter?.onSuccess(appResponseBuilder.buildResponse(appRequest, stepsResults, sessionId))
    }

    private fun addNewStepIntentResult(it: ModalResponse) {
        stepsResults.add(it)
    }


    override fun getAppResponse(): Single<AppResponse> = Single.create {
        appResponseEmitter = it
    }

    @SuppressLint("CheckResult")
    override fun notifyResult(requestCode: Int, resultCode: Int, data: Intent?) {
        flowModal.handleIntentResponse(requestCode, resultCode, data)
    }
}

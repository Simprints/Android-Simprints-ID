package com.simprints.id.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modal.Modal
import com.simprints.id.domain.modal.Modal.*
import com.simprints.id.domain.modal.ModalResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modals.FaceFingerModalFlowImpl
import com.simprints.id.orchestrator.modals.FaceModalFlowImpl
import com.simprints.id.orchestrator.modals.FingerFaceModalFlowImpl
import com.simprints.id.orchestrator.modals.FingerModalFlowImpl
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFace
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFaceFinger
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFinger
import com.simprints.id.orchestrator.modals.builders.AppResponseBuilderForFingerFace
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

class OrchestratorManagerImpl(val modal: Modal,
                              prefs: PreferencesManager) : OrchestratorManager {

    companion object {
        private const val packageName = "com.simprints.id"
    }

    private var appResponseEmitter: SingleEmitter<AppResponse>? = null

    private lateinit var appRequest: AppRequest
    private val results: MutableList<ModalResponse> = mutableListOf()

    private val face by lazy { FaceModalFlowImpl(packageName, appRequest) }
    private val fingerprint by lazy { FingerModalFlowImpl(packageName, appRequest, prefs) }

    private val flowModal by lazy {
        when (modal) {
            FACE -> face
            FINGER -> fingerprint
            FINGER_FACE -> FingerFaceModalFlowImpl(fingerprint, face)
            FACE_FINGER -> FaceFingerModalFlowImpl(fingerprint, face)
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

    private var sessionId: String = ""

    @SuppressLint("CheckResult")
    override fun startFlow(appRequest: AppRequest, sessionId: String): Observable<ModalStepRequest> {
        this.sessionId = sessionId
        this.appRequest = appRequest
        results.clear()

        flowModal.modalResponses
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    results.add(it)
                },
                onComplete = {
                    appResponseEmitter?.onSuccess(appResponseBuilder.buildResponse(appRequest, results, sessionId))
                })

        return flowModal.nextIntent
    }


    override fun getAppResponse(): Single<AppResponse> = Single.create {
        appResponseEmitter = it
    }

    @SuppressLint("CheckResult")
    override fun notifyResult(requestCode: Int, resultCode: Int, data: Intent?) {
        flowModal.handleIntentResponse(requestCode, resultCode, data)
    }
}

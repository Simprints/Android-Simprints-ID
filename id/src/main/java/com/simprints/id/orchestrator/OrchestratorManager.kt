package com.simprints.id.orchestrator

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modals.ModalStepRequest
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Orchestrates the flow of Intents [ModalStepRequest] to execute a specific modality (Face, Finger, etc...).
 * When all intents are terminates, it merges the results to produce a final AppResponse */
interface OrchestratorManager {

    /**
     * Emits [ModalStepRequest]s with the Intents that needs to be launched
     * to progress with a specific ModalFlow
     */
    fun startFlow(appRequest: AppRequest, sessionId:String): Observable<ModalStepRequest>

    /**
     * Emits the final AppResponse when all [ModalStepRequest]s are completed
     */
    fun getAppResponse(): Single<AppResponse>

    /**
     * Handles the results of [ModalStepRequest] (received from onActivityResult)
     */
    fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?)
}

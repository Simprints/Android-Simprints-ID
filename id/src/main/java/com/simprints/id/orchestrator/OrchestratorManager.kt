package com.simprints.id.orchestrator

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modals.ModalStepRequest
import io.reactivex.Observable
import io.reactivex.Single

/**
 * It orchestrates the flow of Intents [ModalStepRequest] to execute a specific modality (Face, Finger, etc...).
 * When all intents are terminates, it merges the results to produce a final AppResponse */
interface OrchestratorManager {

    fun startFlow(appRequest: AppRequest, sessionId:String): Observable<ModalStepRequest>
    fun getAppResponse(): Single<AppResponse>

    fun notifyResult(requestCode: Int, resultCode: Int, data: Intent?)
}

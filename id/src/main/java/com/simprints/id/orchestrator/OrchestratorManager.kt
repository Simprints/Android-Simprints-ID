package com.simprints.id.orchestrator

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

/**
 * Orchestrates the flow of Intents [ModalityStepRequest] to execute a specific modality (Face, Finger, etc...).
 * When all intents are terminates, it merges the results to produce a final AppResponse */
interface OrchestratorManager {

    suspend fun initOrchestrator(appRequest: AppRequest, sessionId:String)

    suspend fun getNextIntent(): ModalityFlow.Request?

    suspend fun getAppResponse(): AppResponse?

    suspend fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?)
}

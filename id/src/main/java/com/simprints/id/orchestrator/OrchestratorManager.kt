package com.simprints.id.orchestrator

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import io.reactivex.subjects.BehaviorSubject

interface OrchestratorManager {

    var finalAppResponse: AppResponse?
    val flow: BehaviorSubject<ModalStep>

    fun startFlow(appRequest: AppRequest, sessionId: String)
    fun notifyResult(requestCode: Int, resultCode: Int, data: Intent?)
}

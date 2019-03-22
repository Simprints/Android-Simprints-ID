package com.simprints.id.orchestrator

import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import io.reactivex.Observable
import io.reactivex.Single

interface OrchestratorManager {

    fun startFlow(appRequest: AppRequest, sessionId:String): Observable<ModalStepRequest>
    fun getAppResponse(): Single<AppResponse>

    fun notifyResult(requestCode: Int, resultCode: Int, data: Intent?)
}

package com.simprints.id.activities.orchestrator

import android.content.Intent
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.moduleapi.app.responses.IAppResponse

interface OrchestratorContract {

    interface View : BaseView<Presenter> {
        fun startNextActivity(requestCode: Int, intent: Intent)

        fun setCancelResultAndFinish()
        fun setResultAndFinish(response: AppResponse)

        fun finishOrchestratorAct()
    }

    interface Presenter : BasePresenter {

        var appRequest: AppRequest
        fun handleResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun fromDomainToAppResponse(response: AppResponse?): IAppResponse?
    }
}

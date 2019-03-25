package com.simprints.id.activities.orchestrator

import android.content.Intent
import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

interface OrchestratorContract {

    interface View : BaseView<Presenter> {
        fun startActivity(requestCode:Int, intent: Intent)

        fun setCancelResultAndFinish()
        fun setResultAndFinish(response: AppResponse)

        fun finishOrchestratorAct()
    }

    interface Presenter : BasePresenter {

        fun handleResult(requestCode: Int, resultCode: Int, data: Intent?)
    }
}

package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.checkLogin.CheckLoginContract
import com.simprints.id.domain.moduleapi.app.requests.AppRequest

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun openLoginActivity(appRequest: AppRequest)
        fun openOrchestratorActivity(appRequest: AppRequest)

        fun getCheckCallingApp(): String
        fun parseRequest(): AppRequest
        fun finishCheckLoginFromIntentActivity()
    }

    interface Presenter : BasePresenter {
        fun setup()
    }
}

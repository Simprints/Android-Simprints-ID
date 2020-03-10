package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BaseView
import com.simprints.id.activities.alert.response.AlertActResponse
import com.simprints.id.activities.checkLogin.CheckLoginContract
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.moduleapi.app.responses.IAppErrorResponse

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun openLoginActivity(appRequest: AppRequest)
        fun openOrchestratorActivity(appRequest: AppRequest)

        fun getCheckCallingApp(): String
        fun parseRequest(): AppRequest
        fun finishCheckLoginFromIntentActivity()
        fun setResultErrorAndFinish(appResponse: IAppErrorResponse)
    }

    interface Presenter {
        suspend fun start()
        suspend fun setup()
        suspend fun checkSignedInStateIfPossible()
        fun onAlertScreenReturn(alertActResponse: AlertActResponse)
        fun onLoginScreenErrorReturn(appErrorResponse: AppErrorResponse)
    }
}

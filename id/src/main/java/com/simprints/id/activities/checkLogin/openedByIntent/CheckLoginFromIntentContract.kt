package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.checkLogin.CheckLoginContract
import com.simprints.id.domain.requests.AppRequest
import com.simprints.id.domain.responses.AppResponse

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun getAppVersionNameFromPackageManager(): String
        fun getDeviceUniqueId(): String

        fun openLoginActivity(appRequest: AppRequest)
        fun openLaunchActivity(appRequest: AppRequest)

        fun getCheckCallingApp(): String
        fun checkCallingAppIsFromKnownSource()
        fun parseAppRequest(): AppRequest
        fun finishCheckLoginFromIntentActivity()
    }

    interface Presenter : BasePresenter {
        fun setup()
        fun handleActivityResult(requestCode: Int, resultCode: Int, appResponse: AppResponse)
    }
}

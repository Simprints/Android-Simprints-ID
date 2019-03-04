package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.checkLogin.CheckLoginContract
import com.simprints.id.domain.requests.Request
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponse

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun getAppVersionNameFromPackageManager(): String
        fun getDeviceUniqueId(): String

        fun openLoginActivity(appRequest: Request)
        fun openLaunchActivity(appRequest: Request)

        fun getCheckCallingApp(): String
        fun checkCallingAppIsFromKnownSource()
        fun parseRequest(): Request
        fun finishCheckLoginFromIntentActivity()
    }

    interface Presenter : BasePresenter {
        fun setup()
        fun handleActivityResult(requestCode: Int, resultCode: Int, appResponse: IClientApiResponse)
    }
}

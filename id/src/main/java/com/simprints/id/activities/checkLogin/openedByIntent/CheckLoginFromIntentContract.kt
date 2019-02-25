package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.checkLogin.CheckLoginContract
import com.simprints.id.domain.request.IdRequest
import com.simprints.id.session.callout.Callout

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun openLoginActivity(legacyApiKey: String)
        fun openLaunchActivity()

        fun getCheckCallingApp(): String
        fun checkCallingAppIsFromKnownSource()
        fun parseIdRequest(): IdRequest
        fun finishCheckLoginFromIntentActivity()
    }

    interface Presenter : BasePresenter {
        fun setup()
        fun handleActivityResult(requestCode: Int, resultCode: Int, returnCallout: Callout)
    }
}

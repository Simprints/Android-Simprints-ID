package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.callout.Callout
import com.simprints.id.model.ALERT_TYPE

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter> {
        fun launchAlertForError(alertType: ALERT_TYPE)
        fun openLoginActivity()
        fun openLaunchActivity()

        fun checkCallingApp()
        fun parseCallout(): Callout
        fun finishAct()
    }

    interface Presenter : BasePresenter {
        fun openNextActivity()
    }
}

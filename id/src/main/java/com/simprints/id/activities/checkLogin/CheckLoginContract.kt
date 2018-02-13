package com.simprints.id.activities.checkLogin

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.domain.callout.Callout
import com.simprints.id.model.ALERT_TYPE

interface CheckLoginContract {

    interface View : BaseView<Presenter> {
        fun launchAlertForError(alertType: ALERT_TYPE)
        fun openLoginActivity()
        fun openRequestLoginActivity()
        fun startActivity(nextActivityClassAfterLogin: Class<out Any>)
        fun checkCallingApp()
        fun parseCallout(): Callout
    }

    interface Presenter : BasePresenter {
        var wasAppOpenedByIntent: Boolean
        fun checkIfUserIsLoggedIn()
    }
}


package com.simprints.id.activities.checkLogin.openedByMainLauncher

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.model.ALERT_TYPE

interface CheckLoginFromMainLauncherContract {

    interface View : BaseView<Presenter> {
        fun launchAlertForError(alertType: ALERT_TYPE)
        fun openDashboardActivity()
        fun openRequestLoginActivity()
    }

    interface Presenter : BasePresenter
}

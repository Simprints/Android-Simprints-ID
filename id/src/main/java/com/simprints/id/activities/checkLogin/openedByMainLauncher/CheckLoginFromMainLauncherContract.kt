package com.simprints.id.activities.checkLogin.openedByMainLauncher

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.checkLogin.CheckLoginContract

interface CheckLoginFromMainLauncherContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun openDashboardActivity()
        fun openRequestLoginActivity()
    }

    interface Presenter : BasePresenter
}

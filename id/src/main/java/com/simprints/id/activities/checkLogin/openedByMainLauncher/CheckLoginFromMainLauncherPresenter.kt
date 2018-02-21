package com.simprints.id.activities.checkLogin.openedByMainLauncher

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.DataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.TimeHelper

class CheckLoginFromMainLauncherPresenter(
    val view: CheckLoginFromMainLauncherContract.View,
    val dataManager: DataManager,
    timeHelper: TimeHelper): CheckLoginPresenter(dataManager, timeHelper), CheckLoginFromMainLauncherContract.Presenter {

    private var started: Boolean = false

    init {
        view.setPresenter(this)
    }

    override fun start() {
        if (!started) {
            started = true
            initSession()
            openNextActivity()
        }
    }

    override fun openActivityForNotSignedInUser() {
        view.openRequestLoginActivity()
    }

    override fun openActivityForSignedInUser() {
        view.openDashboardActivity()
    }

    override fun dbInitFailed(){
        view.launchAlertForError(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    override fun isUserSignedInForStoredProjectId(): Boolean {
        return true
    }
}

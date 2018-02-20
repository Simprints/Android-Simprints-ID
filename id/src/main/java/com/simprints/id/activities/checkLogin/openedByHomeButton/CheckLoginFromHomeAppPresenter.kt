package com.simprints.id.activities.checkLogin.openedByHomeButton

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.DataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.TimeHelper

class CheckLoginFromHomeAppPresenter(
    val view: CheckLoginFromHomeAppContract.View,
    val dataManager: DataManager,
    timeHelper: TimeHelper): CheckLoginPresenter(dataManager, timeHelper), CheckLoginFromHomeAppContract.Presenter {

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

    override fun openActivityForUserNotSignedIn() {
        view.openRequestLoginActivity()
    }

    override fun openActivityForUserSignedIn() {
        view.openDashboardActivity()
    }

    override fun dbInitFailed(){
        view.launchAlertForError(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    override fun isUserSignedInForStoredProjectId(): Boolean {
        return true
    }
}

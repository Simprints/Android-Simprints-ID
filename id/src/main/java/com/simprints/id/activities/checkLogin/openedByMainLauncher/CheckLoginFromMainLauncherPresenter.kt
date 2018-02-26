package com.simprints.id.activities.checkLogin.openedByMainLauncher

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.DataManager
import com.simprints.id.tools.TimeHelper

class CheckLoginFromMainLauncherPresenter(
    val view: CheckLoginFromMainLauncherContract.View,
    val dataManager: DataManager,
    timeHelper: TimeHelper) : CheckLoginPresenter(view, dataManager, timeHelper), CheckLoginFromMainLauncherContract.Presenter {

    init {
        view.setPresenter(this)
    }

    override fun start() {
        checkSignedInStateAndMoveOn()
    }

    override fun handleNotSignedInUser() {
        view.openRequestLoginActivity()
    }

    override fun handleSignedInUser() {
        view.openDashboardActivity()
    }

    override fun isUserSignedInForStoredProjectId(): Boolean {
        return true
    }

    override fun getUserId(): String {
        return dataManager.getSignedInUserIdOrEmpty() // FIXME
    }
}

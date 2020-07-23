package com.simprints.id.activities.checkLogin.openedByMainLauncher

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.di.AppComponent

class CheckLoginFromMainLauncherPresenter(
    val view: CheckLoginFromMainLauncherContract.View,
    component: AppComponent) : CheckLoginPresenter(view, component), CheckLoginFromMainLauncherContract.Presenter {

    init {
        component.inject(this)
    }

    override suspend fun start() {
        checkSignedInStateAndMoveOn()
    }

    override fun handleNotSignedInUser() {
        view.openRequestLoginActivity()
    }

    override suspend fun handleSignedInUser() {
        super.handleSignedInUser()
        view.openDashboardActivity()
    }

    override fun isProjectIdStoredAndMatches(): Boolean = loginInfoManager.getSignedInProjectIdOrEmpty().isNotEmpty()

    override fun isUserIdStoredAndMatches(): Boolean = loginInfoManager.getSignedInUserIdOrEmpty().isNotEmpty()
}

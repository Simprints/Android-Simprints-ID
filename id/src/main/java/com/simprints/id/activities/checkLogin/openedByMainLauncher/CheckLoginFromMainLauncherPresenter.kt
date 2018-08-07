package com.simprints.id.activities.checkLogin.openedByMainLauncher

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import com.simprints.id.data.analytics.events.SessionEventsManager
import com.simprints.id.di.AppComponent
import javax.inject.Inject

class CheckLoginFromMainLauncherPresenter(
    val view: CheckLoginFromMainLauncherContract.View,
    component: AppComponent) : CheckLoginPresenter(view, component), CheckLoginFromMainLauncherContract.Presenter {

    @Inject lateinit var sessionEventManager: SessionEventsManager

    init {
        component.inject(this)
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

    override fun isProjectIdStoredAndMatches(): Boolean = loginInfoManager.getSignedInProjectIdOrEmpty().isNotEmpty()

    override fun isUserIdStoredAndMatches(): Boolean = loginInfoManager.getSignedInUserIdOrEmpty().isNotEmpty()
}

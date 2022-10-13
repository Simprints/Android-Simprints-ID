package com.simprints.id.activities.checkLogin.openedByMainLauncher

import com.simprints.id.activities.checkLogin.CheckLoginPresenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class CheckLoginFromMainLauncherPresenter @AssistedInject constructor(
    @Assisted private val view: CheckLoginFromMainLauncherContract.View,
) : CheckLoginPresenter(view), CheckLoginFromMainLauncherContract.Presenter {

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

    override fun isProjectIdStoredAndMatches(): Boolean =
        loginManager.getSignedInProjectIdOrEmpty().isNotEmpty()

    override fun isUserIdStoredAndMatches(): Boolean =
        loginManager.getSignedInUserIdOrEmpty().isNotEmpty()
}

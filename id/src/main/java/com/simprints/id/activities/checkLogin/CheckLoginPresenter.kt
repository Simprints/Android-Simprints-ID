package com.simprints.id.activities.checkLogin

import com.simprints.id.data.DataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.StringsUtils

abstract class CheckLoginPresenter (
    private val view: CheckLoginContract.View,
    private val dataManager: DataManager,
    private val timeHelper: TimeHelper) {

    init {
        initSession()
    }

    private fun initSession() {
        dataManager.initializeSessionState(newSessionId(), timeHelper.msSinceBoot())
    }

    private fun newSessionId(): String {
        return StringsUtils.randomUUID()
    }

    protected fun checkSignedInStateAndMoveOn() {
        dataManager.initialiseDb().subscribe({
            if (isUserSignedIn()) {
                handleSignedInUser()
            } else {
                handleNotSignedInUser()
            }
        }, {
            view.launchAlertForError(ALERT_TYPE.UNEXPECTED_ERROR)
         })
    }

    abstract fun handleSignedInUser()
    abstract fun handleNotSignedInUser()

    private fun isEncryptedProjectSecretPresent(): Boolean = dataManager.getEncryptedProjectSecretOrEmpty().isNotEmpty()
    private fun isProjectIdStored(): Boolean = dataManager.getSignedInProjectIdOrEmpty().isNotEmpty()
    private fun isFirebaseTokenValid(): Boolean = dataManager.isSignedIn(dataManager.getSignedInProjectIdOrEmpty(), getUserId())
    abstract fun getUserId(): String

    private fun isUserSignedIn(): Boolean =
        isEncryptedProjectSecretPresent() && isProjectIdStored() && isFirebaseTokenValid()
}

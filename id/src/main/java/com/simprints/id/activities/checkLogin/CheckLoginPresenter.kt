package com.simprints.id.activities.checkLogin

import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
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

    protected fun checkSignedInStateAndMoveOn() {
        if (isUserSignedIn()) {
            handleSignedInUser()
        } else {
            handleNotSignedInUser()
        }
    }

    abstract fun handleNotSignedInUser()

    open fun handleSignedInUser() {
        initDbManager(dataManager.getSignedInProjectIdOrEmpty())
    }

    private fun initDbManager(projectId: String) {
        if (!dataManager.isDbInitialised(projectId)) {
            try {
                dataManager.initialiseDb(projectId)
            } catch (error: UninitializedDataManagerError) {
                dataManager.logError(error)
                dbInitFailed()
            }
        }
    }

    private fun dbInitFailed() {
        view.launchAlertForError(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    private fun isUserSignedIn(): Boolean =
        isEncryptedProjectSecretPresent() && isProjectIdStored() && isFirebaseTokenValid() && isUserSignedInForStoredProjectId()

    private fun isEncryptedProjectSecretPresent(): Boolean = dataManager.getEncryptedProjectSecretOrEmpty().isNotEmpty()
    private fun isProjectIdStored(): Boolean = dataManager.getSignedInProjectIdOrEmpty().isNotEmpty()
    private fun isFirebaseTokenValid(): Boolean = dataManager.isSignedIn(dataManager.getSignedInProjectIdOrEmpty()) || true

    abstract fun isUserSignedInForStoredProjectId(): Boolean

    private fun initSession() {
        dataManager.initializeSessionState(newSessionId(), timeHelper.msSinceBoot())
    }

    private fun newSessionId(): String {
        return StringsUtils.randomUUID()
    }
}

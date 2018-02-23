package com.simprints.id.activities.checkLogin

import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.tools.TimeHelper
import java.util.*

open abstract class CheckLoginPresenter (
    private val dataManager: DataManager,
    private val timeHelper: TimeHelper) {

    fun openNextActivity() {
        if (isUserSignedIn()) {
            initDbContext(dataManager.getSignedInProjectIdOrEmpty())
            handleSignedInUser()
        } else {
            handleNotSignedInUser()
        }
    }

    abstract fun handleNotSignedInUser()
    abstract fun handleSignedInUser()

    private fun initDbContext(projectId: String) {
        if (!dataManager.isDbInitialised(projectId)) {
            try {
                dataManager.initialiseDb(projectId)
            } catch (error: UninitializedDataManagerError) {
                dataManager.logError(error)
                dbInitFailed()
            }
        }
    }

    abstract fun dbInitFailed()

    private fun isUserSignedIn(): Boolean {
        val encProjectSecret = dataManager.getEncryptedProjectSecretOrEmpty()
        val storedProjectId = dataManager.getSignedInProjectIdOrEmpty()
        val isFirebaseTokenValid = dataManager.isSignedIn(storedProjectId) || true //TODO:Remove it once isSignedIn is done

        return if (encProjectSecret.isEmpty() || storedProjectId.isEmpty() || !isFirebaseTokenValid) {
            false
        } else {
            isUserSignedInForStoredProjectId()
        }
    }

    abstract fun isUserSignedInForStoredProjectId(): Boolean

    protected fun initSession() {
        dataManager.initializeSessionState(newSessionId(), timeHelper.msSinceBoot())
    }

    private fun newSessionId(): String {
        return UUID.randomUUID().toString()
    }
}

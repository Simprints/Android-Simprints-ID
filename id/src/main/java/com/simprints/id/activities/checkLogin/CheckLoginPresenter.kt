package com.simprints.id.activities.checkLogin

import com.simprints.id.data.DataManager
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.tools.TimeHelper
import java.util.*

open class CheckLoginPresenter (
    private val dataManager: DataManager,
    private val timeHelper: TimeHelper) {

    fun openNextActivity() {
        if (isUserSignedIn()) {
            initDbContext()
            handleSignedInUser()
        } else {
            handleNotSignedInUser()
        }
    }

    protected open fun handleNotSignedInUser() {
        throw Exception("Not overridden")
    }

    protected open fun handleSignedInUser() {
        throw Exception("Not overridden")
    }

    private fun initDbContext() {
        if (!dataManager.isDbInitialised()) {
            try {
                dataManager.initialiseDb()
            } catch (error: UninitializedDataManagerError) {
                dataManager.logError(error)
                dbInitFailed()
            }
        }
    }

    protected open fun dbInitFailed() {
        throw Exception("Not overridden")
    }

    private fun isUserSignedIn(): Boolean {
        val encProjectSecret = dataManager.getEncryptedProjectSecretOrEmpty()
        val storedProjectId = dataManager.getSignedInProjectIdOrEmpty()
        val userId = getUserId()
        val isFirebaseTokenValid = dataManager.isSignedIn(storedProjectId, userId) //TODO:Remove it once isSignedIn is done
    
        return if (encProjectSecret.isEmpty() || storedProjectId.isEmpty() || !isFirebaseTokenValid) {
            false
        } else {
            isUserSignedInForStoredProjectId()
        }
    }

    protected open fun getUserId(): String {
        throw Exception("Not overridden")
    }

    protected open fun isUserSignedInForStoredProjectId(): Boolean {
        throw Exception("Not overridden")
    }

    protected fun initSession() {
        dataManager.initializeSessionState(newSessionId(), timeHelper.msSinceBoot())
    }

    private fun newSessionId(): String {
        return UUID.randomUUID().toString()
    }
}

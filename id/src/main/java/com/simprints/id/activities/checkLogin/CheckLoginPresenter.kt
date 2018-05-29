package com.simprints.id.activities.checkLogin

import com.simprints.id.data.DataManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.exceptions.unsafe.RealmUninitialisedError
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.StringsUtils

abstract class CheckLoginPresenter(
    private val view: CheckLoginContract.View,
    private val dataManager: DataManager,
    private val secureDataManager: SecureDataManager,
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
        try {
            checkSignedInOrThrow()
            handleSignedInUser()
        } catch (e: Throwable) {
            when (e) {
                is DifferentProjectIdSignedInException -> view.openAlertActivityForError(ALERT_TYPE.INVALID_PROJECT_ID)
                is DifferentUserIdSignedInException -> view.openAlertActivityForError(ALERT_TYPE.INVALID_USER_ID)
                is NotSignedInException -> handleNotSignedInUser()
                else -> {
                    dataManager.analytics.logThrowable(e)
                    view.openAlertActivityForError(ALERT_TYPE.UNEXPECTED_ERROR)
                }
            }
        }
    }

    abstract fun handleSignedInUser()
    abstract fun handleNotSignedInUser()

    /**
     * @throws DifferentProjectIdSignedInException
     * @throws DifferentUserIdSignedInException
     * @throws NotSignedInException
     */
    private fun checkSignedInOrThrow() {
        val isUserSignedIn =
            isEncryptedProjectSecretPresent() &&
            isProjectIdStoredAndMatches() &&
            isLocalKeyValid(dataManager.loginInfo.getSignedInProjectIdOrEmpty()) &&
            isUserIdStoredAndMatches() &&
            isFirebaseTokenValid()

        if (!isUserSignedIn)
            throw NotSignedInException()
    }

    private fun isEncryptedProjectSecretPresent(): Boolean = dataManager.loginInfo.getEncryptedProjectSecretOrEmpty().isNotEmpty()
    private fun isFirebaseTokenValid(): Boolean = dataManager.db.isSignedIn(dataManager.loginInfo.getSignedInProjectIdOrEmpty(), dataManager.loginInfo.getSignedInUserIdOrEmpty())
    private fun isLocalKeyValid(projectId: String): Boolean = try {
        secureDataManager.getLocalDbKeyOrThrow(projectId)
        true
    } catch (t: Throwable) {
        false
    }

    /** @throws DifferentProjectIdSignedInException */
    abstract fun isProjectIdStoredAndMatches(): Boolean

    /** @throws DifferentUserIdSignedInException */
    abstract fun isUserIdStoredAndMatches(): Boolean
}

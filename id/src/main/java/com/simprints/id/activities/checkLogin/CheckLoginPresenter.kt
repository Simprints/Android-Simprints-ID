package com.simprints.id.activities.checkLogin

import com.simprints.id.data.DataManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.StringsUtils
import io.reactivex.rxkotlin.subscribeBy

abstract class CheckLoginPresenter(
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
        try {
            checkSignedInOrThrow()
            val projectId = dataManager.getSignedInProjectIdOrEmpty()
            dataManager.getLocalKeyAndSignInToLocal(projectId).subscribeBy(onComplete = {
                handleSignedInUser()
            })
        } catch (e: Throwable) {
            when (e) {
                is DifferentProjectIdSignedInException -> view.openAlertActivityForError(ALERT_TYPE.INVALID_PROJECT_ID)
                is DifferentUserIdSignedInException -> view.openAlertActivityForError(ALERT_TYPE.INVALID_USER_ID)
                is NotSignedInException -> handleNotSignedInUser()
                else -> {
                    dataManager.logThrowable(e); view.openAlertActivityForError(ALERT_TYPE.UNEXPECTED_ERROR)
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
        if (!isEncryptedProjectSecretPresent() ||
            !isProjectIdStoredAndMatches() ||
            !isUserIdStoredAndMatches() ||
            !isFirebaseTokenValid())
            throw NotSignedInException()
    }

    private fun isEncryptedProjectSecretPresent(): Boolean = dataManager.getEncryptedProjectSecretOrEmpty().isNotEmpty()
    private fun isFirebaseTokenValid(): Boolean = dataManager.isSignedIn(dataManager.getSignedInProjectIdOrEmpty(), dataManager.getSignedInUserIdOrEmpty())

    /** @throws DifferentProjectIdSignedInException */
    abstract fun isProjectIdStoredAndMatches(): Boolean

    /** @throws DifferentUserIdSignedInException */
    abstract fun isUserIdStoredAndMatches(): Boolean
}

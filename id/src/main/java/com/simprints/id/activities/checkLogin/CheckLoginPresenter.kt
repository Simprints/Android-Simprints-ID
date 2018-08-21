package com.simprints.id.activities.checkLogin

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.session.sessionParameters.extractors.SessionParametersExtractor
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.StringsUtils
import javax.inject.Inject

abstract class CheckLoginPresenter(
    private val view: CheckLoginContract.View,
    component: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var secureDataManager: SecureDataManager
    @Inject lateinit var sessionParametersExtractor: SessionParametersExtractor

    init {
        component.inject(this)
        initSession()
    }

    private fun initSession() {
        preferencesManager.initializeSessionState(newSessionId(), timeHelper.now())
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
                    e.printStackTrace()
                    analyticsManager.logThrowable(e)
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
            isLocalKeyValid(loginInfoManager.getSignedInProjectIdOrEmpty()) &&
            isUserIdStoredAndMatches() &&
            isFirebaseTokenValid()

        if (!isUserSignedIn)
            throw NotSignedInException()
    }

    private fun isEncryptedProjectSecretPresent(): Boolean = loginInfoManager.getEncryptedProjectSecretOrEmpty().isNotEmpty()
    private fun isFirebaseTokenValid(): Boolean = remoteDbManager.isSignedIn(loginInfoManager.getSignedInProjectIdOrEmpty(), loginInfoManager.getSignedInUserIdOrEmpty())
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

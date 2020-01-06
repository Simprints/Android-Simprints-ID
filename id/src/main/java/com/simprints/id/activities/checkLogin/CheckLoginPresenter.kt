package com.simprints.id.activities.checkLogin

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.alert.AlertType.*
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.tools.TimeHelper
import timber.log.Timber
import javax.inject.Inject

abstract class CheckLoginPresenter(
    private val view: CheckLoginContract.View,
    component: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var secureDataManager: SecureLocalDbKeyProvider
    @Inject lateinit var syncManager: SyncManager

    init {
        component.inject(this)
    }

    protected fun checkSignedInStateAndMoveOn() {
        try {
            checkSignedInOrThrow()
            handleSignedInUser()
        } catch (t: Throwable) {
            Timber.e(t)

            when (t) {
                is DifferentProjectIdSignedInException -> view.openAlertActivityForError(DIFFERENT_PROJECT_ID_SIGNED_IN)
                is DifferentUserIdSignedInException -> view.openAlertActivityForError(DIFFERENT_USER_ID_SIGNED_IN)
                is NotSignedInException -> handleNotSignedInUser().also {
                    syncManager.cancelBackgroundSyncs()
                }
                else -> {
                    Timber.e(t)
                    crashReportManager.logExceptionOrSafeException(t)
                    view.openAlertActivityForError(UNEXPECTED_ERROR)
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

        if (!isUserSignedIn) {
            throw NotSignedInException()
        }
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

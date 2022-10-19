package com.simprints.id.activities.checkLogin

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.domain.alert.AlertType.*
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.services.sync.SyncManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject

abstract class CheckLoginPresenter(
    private val view: CheckLoginContract.View,
) {

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var timeHelper: TimeHelper

    @Inject
    lateinit var loginManager: LoginManager

    @Inject
    lateinit var secureDataManager: SecurityManager

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var securityStateRepository: SecurityStateRepository
    protected suspend fun checkSignedInStateAndMoveOn() {
        try {
            checkSignedInOrThrow()
            handleSignedInUser()
        } catch (t: Throwable) {
            when (t) {
                is DifferentProjectIdSignedInException -> view.openAlertActivityForError(
                    DIFFERENT_PROJECT_ID_SIGNED_IN
                )
                is DifferentUserIdSignedInException -> view.openAlertActivityForError(
                    DIFFERENT_USER_ID_SIGNED_IN
                )
                is NotSignedInException -> handleNotSignedInUser().also {
                    syncManager.cancelBackgroundSyncs()
                }
                else -> {
                    Simber.e(t)
                    view.openAlertActivityForError(UNEXPECTED_ERROR)
                }
            }
        }
    }

    open suspend fun handleSignedInUser() {
        checkStatusForDeviceAndProject()
    }

    private fun checkStatusForDeviceAndProject() {
        val status = securityStateRepository.getSecurityStatusFromLocal()
        if (status.isCompromisedOrProjectEnded())
            handleNotSignedInUser()
    }

    abstract fun handleNotSignedInUser()

    /**
     * @throws DifferentProjectIdSignedInException
     * @throws DifferentUserIdSignedInException
     * @throws NotSignedInException
     */
    private fun checkSignedInOrThrow() {
        val isUserSignedIn =
            isProjectIdStoredAndMatches() &&
                isLocalKeyValid(loginManager.getSignedInProjectIdOrEmpty()) &&
                isUserIdStoredAndMatches() &&
                isFirebaseTokenValid()

        if (!isUserSignedIn) {
            throw NotSignedInException()
        }
    }

    private fun isFirebaseTokenValid(): Boolean = loginManager.isSignedIn(
        loginManager.getSignedInProjectIdOrEmpty(),
        loginManager.getSignedInUserIdOrEmpty()
    )

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

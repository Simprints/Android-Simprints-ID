package com.simprints.id.activities.checkLogin

import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.alert.AlertType.DIFFERENT_PROJECT_ID
import com.simprints.id.alert.AlertType.DIFFERENT_USER_ID
import com.simprints.id.alert.AlertType.UNEXPECTED_ERROR
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdSignedInException
import com.simprints.id.exceptions.safe.secure.DifferentUserIdSignedInException
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.exceptions.safe.secure.ProjectPausedException
import com.simprints.id.services.sync.SyncManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
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
    lateinit var authStore: AuthStore

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
                is DifferentProjectIdSignedInException -> {
                    view.openAlertActivityForError(DIFFERENT_PROJECT_ID)
                }

                is DifferentUserIdSignedInException -> {
                    view.openAlertActivityForError(DIFFERENT_USER_ID)
                }

                is NotSignedInException -> handleNotSignedInUser().also {
                    syncManager.cancelBackgroundSyncs()
                }

                is ProjectPausedException -> handlePausedProject().also {
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
        when {
            status == SecurityState.Status.PAUSED -> throw ProjectPausedException()
            status.isCompromisedOrProjectEnded() -> handleNotSignedInUser()
        }
    }

    abstract fun handlePausedProject()
    abstract fun handleNotSignedInUser()

    /**
     * @throws DifferentProjectIdSignedInException
     * @throws DifferentUserIdSignedInException
     * @throws NotSignedInException
     */
    private fun checkSignedInOrThrow() {
        val isUserSignedIn =
            isProjectIdStoredAndMatches() &&
                isLocalKeyValid(authStore.signedInProjectId) &&
                isUserIdStoredAndMatches() &&
                isFirebaseTokenValid()

        if (!isUserSignedIn) {
            throw NotSignedInException()
        }
    }

    private fun isFirebaseTokenValid(): Boolean = authStore.isSignedIn(
        authStore.signedInProjectId,
        authStore.signedInUserId
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

package com.simprints.feature.logincheck

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.feature.logincheck.usecases.AddAuthorizationEventUseCase
import com.simprints.feature.logincheck.usecases.ExtractCrashKeysUseCase
import com.simprints.feature.logincheck.usecases.ExtractParametersForAnalyticsUseCase
import com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase
import com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.MISMATCHED_PROJECT_ID
import com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN
import com.simprints.feature.logincheck.usecases.IsUserSignedInUseCase.SignedInState.SIGNED_IN
import com.simprints.feature.logincheck.usecases.ReportActionRequestEventsUseCase
import com.simprints.feature.logincheck.usecases.StartBackgroundSyncUseCase
import com.simprints.feature.logincheck.usecases.UpdateProjectInCurrentSessionUseCase
import com.simprints.feature.logincheck.usecases.UpdateSessionScopePayloadUseCase
import com.simprints.feature.logincheck.usecases.UpdateStoredUserIdUseCase
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationScheduler
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class LoginCheckViewModel @Inject internal constructor(
    private val rootManager: SecurityManager,
    private val reportActionRequestEvents: ReportActionRequestEventsUseCase,
    private val extractParametersForAnalytics: ExtractParametersForAnalyticsUseCase,
    private val extractParametersForCrashReport: ExtractCrashKeysUseCase,
    private val addAuthorizationEvent: AddAuthorizationEventUseCase,
    private val isUserSignedIn: IsUserSignedInUseCase,
    private val configManager: ConfigManager,
    private val startBackgroundSync: StartBackgroundSyncUseCase,
    private val syncOrchestrator: SyncOrchestrator,
    private val updateDatabaseCountsInCurrentSession: UpdateSessionScopePayloadUseCase,
    private val updateProjectInCurrentSession: UpdateProjectInCurrentSessionUseCase,
    private val updateStoredUserId: UpdateStoredUserIdUseCase,
    private val realmToRoomMigrationScheduler: RealmToRoomMigrationScheduler,
) : ViewModel() {
    private var cachedRequest: ActionRequest? = null
    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)

    val showAlert: LiveData<LiveDataEventWithContent<LoginCheckError>>
        get() = _showAlert
    private val _showAlert = MutableLiveData<LiveDataEventWithContent<LoginCheckError>>()

    val showLoginFlow: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _showLoginFlow
    private val _showLoginFlow = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()

    val proceedWithAction: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _proceedWithAction
    private val _proceedWithAction = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()

    val returnLoginNotComplete: LiveData<LiveDataEvent>
        get() = _returnLoginNotComplete
    private val _returnLoginNotComplete = MutableLiveData<LiveDataEvent>()

    fun isDeviceSafe(): Boolean = try {
        rootManager.checkIfDeviceIsRooted()
        true
    } catch (e: RootedDeviceException) {
        Simber.e("Rooted device detected on login check", e, tag = LOGIN)
        _showAlert.send(LoginCheckError.ROOTED_DEVICE)
        false
    }

    suspend fun validateSignInAndProceed(actionRequest: ActionRequest) {
        reportActionRequestEvents(actionRequest)
        extractParametersForAnalytics.invoke(actionRequest)

        when (isUserSignedIn(actionRequest)) {
            MISMATCHED_PROJECT_ID -> _showAlert.send(LoginCheckError.DIFFERENT_PROJECT_ID)
            NOT_SIGNED_IN -> startSignInAttempt(actionRequest)
            SIGNED_IN -> validateProjectAndProceed(actionRequest)
        }
    }

    private suspend fun startSignInAttempt(actionRequest: ActionRequest) {
        // Followup action should not trigger login, since there can not be a valid session open.
        if (loginAlreadyTried.get() || actionRequest is ActionRequest.FollowUpAction) {
            _returnLoginNotComplete.send()
            return
        }
        Simber.i("Start log-in attempt", tag = LOGIN)
        addAuthorizationEvent(actionRequest, false)
        cachedRequest = actionRequest
        loginAlreadyTried.set(true)

        syncOrchestrator.cancelBackgroundWork()

        _showLoginFlow.send(actionRequest)
    }

    fun handleLoginResult(result: LoginResult) = viewModelScope.launch {
        Simber.i("Log-in result: $result", tag = LOGIN)
        val requestAction = cachedRequest?.takeIf { result.isSuccess }
        if (requestAction != null) {
            validateProjectAndProceed(requestAction)
        } else {
            when (result.error) {
                null, LoginError.LoginNotCompleted -> {
                    if (cachedRequest != null) {
                        _returnLoginNotComplete.send()
                    } else {
                        // there is no other reasonable way to handle the error
                        _showAlert.send(LoginCheckError.UNEXPECTED_LOGIN_ERROR)
                    }
                }

                LoginError.IntegrityServiceError -> _showAlert.send(LoginCheckError.INTEGRITY_SERVICE_ERROR)
                LoginError.MissingPlayServices -> _showAlert.send(LoginCheckError.MISSING_GOOGLE_PLAY_SERVICES)
                LoginError.OutdatedPlayServices -> _showAlert.send(LoginCheckError.GOOGLE_PLAY_SERVICES_OUTDATED)
                LoginError.MissingOrOutdatedPlayServices -> _showAlert.send(LoginCheckError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP)
                LoginError.Unknown -> _showAlert.send(LoginCheckError.UNEXPECTED_LOGIN_ERROR)
            }
        }
    }

    private suspend fun validateProjectAndProceed(actionRequest: ActionRequest) {
        when (configManager.getProject()?.state) {
            null, ProjectState.PROJECT_ENDING -> _showAlert.send(LoginCheckError.PROJECT_ENDING)
            ProjectState.PROJECT_PAUSED -> _showAlert.send(LoginCheckError.PROJECT_PAUSED)
            ProjectState.PROJECT_ENDED -> startSignInAttempt(actionRequest)
            ProjectState.RUNNING -> proceedWithAction(actionRequest)
        }
    }

    private fun proceedWithAction(actionRequest: ActionRequest) = viewModelScope.launch {
        updateProjectInCurrentSession()
        updateStoredUserId(actionRequest.userId)
        awaitAll(
            async { updateDatabaseCountsInCurrentSession() },
            async { addAuthorizationEvent(actionRequest, true) },
            async { extractParametersForCrashReport(actionRequest) },
        )
        // Schedule Realm-to-Room migration after successful login, if needed.
        // This avoids down-syncing data into Realm then migrate to room instead set Room  immediately the active db.
        realmToRoomMigrationScheduler.scheduleMigrationWorkerIfNeeded()
        startBackgroundSync()
        _proceedWithAction.send(actionRequest)
    }
}

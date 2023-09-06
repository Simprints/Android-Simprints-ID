package com.simprints.feature.clientapi.logincheck

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.clientapi.logincheck.usecase.CancelBackgroundSyncUseCase
import com.simprints.feature.clientapi.logincheck.usecase.ExtractCrashKeysUseCase
import com.simprints.feature.clientapi.logincheck.usecase.ExtractParametersForAnalyticsUseCase
import com.simprints.feature.clientapi.logincheck.usecase.IsUserSignedInUseCase
import com.simprints.feature.clientapi.logincheck.usecase.IsUserSignedInUseCase.SignedInState.MISMATCHED_PROJECT_ID
import com.simprints.feature.clientapi.logincheck.usecase.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN
import com.simprints.feature.clientapi.logincheck.usecase.IsUserSignedInUseCase.SignedInState.SIGNED_IN
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.models.ClientApiResultError
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.feature.clientapi.logincheck.usecase.GetProjectStateUseCase
import com.simprints.feature.clientapi.logincheck.usecase.GetProjectStateUseCase.ProjectState
import com.simprints.feature.clientapi.logincheck.usecase.ReportActionRequestEventsUseCase
import com.simprints.feature.clientapi.logincheck.usecase.UpdateDatabaseCountsInCurrentSessionUseCase
import com.simprints.feature.clientapi.logincheck.usecase.UpdateProjectInCurrentSessionUseCase
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.moduleapi.app.responses.IAppErrorReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class LoginCheckViewModel @Inject constructor(
    private val rootManager: SecurityManager,
    private val intentMapper: IntentToActionMapper,
    private val clientSessionManager: ClientSessionManager,
    private val reportActionRequestEvents: ReportActionRequestEventsUseCase,
    private val extractParametersForAnalytics: ExtractParametersForAnalyticsUseCase,
    private val extractParametersForCrashReport: ExtractCrashKeysUseCase,
    private val isUserSignedIn: IsUserSignedInUseCase,
    private val getProjectStatus: GetProjectStateUseCase,
    private val cancelBackgroundSync: CancelBackgroundSyncUseCase,
    private val updateDatabaseCountsInCurrentSession: UpdateDatabaseCountsInCurrentSessionUseCase,
    private val updateProjectInCurrentSession: UpdateProjectInCurrentSessionUseCase,
) : ViewModel() {

    private var cachedRequest: ActionRequest? = null
    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)

    val showAlert: LiveData<LiveDataEventWithContent<ClientApiError>>
        get() = _showAlert
    private val _showAlert = MutableLiveData<LiveDataEventWithContent<ClientApiError>>()

    val showLoginFlow: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _showLoginFlow
    private val _showLoginFlow = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()


    val proceedWithAction: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _proceedWithAction
    private val _proceedWithAction = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()

    val returnErrorResponse: LiveData<LiveDataEventWithContent<Pair<ActionRequest, ClientApiResultError>>>
        get() = _returnErrorResponse
    private val _returnErrorResponse = MutableLiveData<LiveDataEventWithContent<Pair<ActionRequest, ClientApiResultError>>>()


    fun handleIntent(action: String, extras: Map<String, Any>) = viewModelScope.launch {
        try {
            rootManager.checkIfDeviceIsRooted()
            validateActionAndProceed(action, extras)
        } catch (e: RootedDeviceException) {
            Simber.e(e)
            _showAlert.send(ClientApiError.ROOTED_DEVICE)
        }
    }

    private suspend fun validateActionAndProceed(action: String, extras: Map<String, Any>) = try {
        val actionRequest = intentMapper(action, extras)

        reportActionRequestEvents(actionRequest)
        extractParametersForAnalytics.invoke(actionRequest)

        validateSignInAndProceed(actionRequest)
    } catch (validationException: InvalidRequestException) {
        Simber.e(validationException)
        clientSessionManager.addInvalidIntentEvent(action, extras)
        _showAlert.send(validationException.error)
    }

    private suspend fun validateSignInAndProceed(actionRequest: ActionRequest) {
        when (isUserSignedIn(actionRequest)) {
            MISMATCHED_PROJECT_ID -> _showAlert.send(ClientApiError.DIFFERENT_PROJECT_ID)
            NOT_SIGNED_IN -> startSignInAttempt(actionRequest)
            SIGNED_IN -> validateProjectAndProceed(actionRequest)
        }
    }

    private suspend fun startSignInAttempt(actionRequest: ActionRequest) {
        // Followup action should not trigger login, since there can not be a valid session open.
        if (loginAlreadyTried.get() || actionRequest is ActionRequest.FollowUpAction) {
            _returnErrorResponse.send(actionRequest to ClientApiResultError(IAppErrorReason.LOGIN_NOT_COMPLETE))
            return
        }
        clientSessionManager.addAuthorizationEvent(actionRequest, false)
        cachedRequest = actionRequest
        loginAlreadyTried.set(true)

        cancelBackgroundSync.invoke()

        _showLoginFlow.send(actionRequest)
    }

    fun handleLoginResult(result: LoginResult) = viewModelScope.launch {
        val requestAction = cachedRequest?.takeIf { result.isSuccess }
        if (requestAction != null) {
            validateProjectAndProceed(requestAction)
        } else {
            when (result.error) {
                null, LoginError.LoginNotCompleted -> {
                    if (cachedRequest != null) {
                        _returnErrorResponse.send(cachedRequest!! to ClientApiResultError(IAppErrorReason.LOGIN_NOT_COMPLETE))
                    } else {
                        // there is no other reasonable way to handle the error
                        _showAlert.send(ClientApiError.UNEXPECTED_LOGIN_ERROR)
                    }
                }

                LoginError.IntegrityServiceError -> _showAlert.send(ClientApiError.INTEGRITY_SERVICE_ERROR)
                LoginError.MissingPlayServices -> _showAlert.send(ClientApiError.MISSING_GOOGLE_PLAY_SERVICES)
                LoginError.OutdatedPlayServices -> _showAlert.send(ClientApiError.GOOGLE_PLAY_SERVICES_OUTDATED)
                LoginError.MissingOrOutdatedPlayServices -> _showAlert.send(ClientApiError.MISSING_OR_OUTDATED_GOOGLE_PLAY_STORE_APP)
                LoginError.Unknown -> _showAlert.send(ClientApiError.UNEXPECTED_LOGIN_ERROR)
            }
        }
    }

    private suspend fun validateProjectAndProceed(actionRequest: ActionRequest) {
        when (getProjectStatus()) {
            ProjectState.PAUSED -> _showAlert.send(ClientApiError.PROJECT_PAUSED)
            ProjectState.ENDING -> _showAlert.send(ClientApiError.PROJECT_ENDING)
            ProjectState.ENDED -> startSignInAttempt(actionRequest)
            ProjectState.ACTIVE -> proceedWithAction(actionRequest)
        }
    }

    private suspend fun proceedWithAction(actionRequest: ActionRequest) = viewModelScope.launch {
        // TODO add special case for confirmation action

        updateProjectInCurrentSession()
        awaitAll(
            async { updateDatabaseCountsInCurrentSession() },
            async { clientSessionManager.addAuthorizationEvent(actionRequest, true) },
            async { extractParametersForCrashReport(actionRequest) }
        )

        _proceedWithAction.send(actionRequest) // TODO replace with user flow builder
    }

}

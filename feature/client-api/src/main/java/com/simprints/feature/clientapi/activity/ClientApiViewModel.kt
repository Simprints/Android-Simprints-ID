package com.simprints.feature.clientapi.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.clientapi.activity.usecases.CancelBackgroundSyncUseCase
import com.simprints.feature.clientapi.activity.usecases.ExtractCrashKeysUseCase
import com.simprints.feature.clientapi.activity.usecases.ExtractParametersForAnalyticsUseCase
import com.simprints.feature.clientapi.activity.usecases.IsFlowCompletedWithErrorUseCase
import com.simprints.feature.clientapi.activity.usecases.IsUserSignedInUseCase
import com.simprints.feature.clientapi.activity.usecases.IsUserSignedInUseCase.SignedInState.MISMATCHED_PROJECT_ID
import com.simprints.feature.clientapi.activity.usecases.IsUserSignedInUseCase.SignedInState.NOT_SIGNED_IN
import com.simprints.feature.clientapi.activity.usecases.IsUserSignedInUseCase.SignedInState.SIGNED_IN
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.models.ActionResponse
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.models.ClientApiResultError
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.feature.clientapi.session.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.session.GetEnrolmentCreationEventForSubjectUseCase
import com.simprints.feature.clientapi.session.GetEventJsonForSessionUseCase
import com.simprints.feature.clientapi.session.GetProjectStateUseCase
import com.simprints.feature.clientapi.session.GetProjectStateUseCase.ProjectState
import com.simprints.feature.clientapi.session.ReportActionRequestEventsUseCase
import com.simprints.feature.clientapi.session.UpdateDatabaseCountsInCurrentSessionUseCase
import com.simprints.feature.clientapi.session.UpdateProjectInCurrentSessionUseCase
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.moduleapi.app.responses.IAppConfirmationResponse
import com.simprints.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppIdentifyResponse
import com.simprints.moduleapi.app.responses.IAppRefusalFormResponse
import com.simprints.moduleapi.app.responses.IAppVerifyResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class ClientApiViewModel @Inject constructor(
    private val rootManager: SecurityManager,
    private val intentMapper: IntentToActionMapper,
    private val clientSessionManager: ClientSessionManager,
    private val reportActionRequestEvents: ReportActionRequestEventsUseCase,
    private val extractParametersForAnalytics: ExtractParametersForAnalyticsUseCase,
    private val extractParametersForCrashReport: ExtractCrashKeysUseCase,
    private val isUserSignedIn: IsUserSignedInUseCase,
    private val getProjectStatus: GetProjectStateUseCase,
    private val getEventJsonForSession: GetEventJsonForSessionUseCase,
    private val cancelBackgroundSync: CancelBackgroundSyncUseCase,
    private val updateDatabaseCountsInCurrentSession: UpdateDatabaseCountsInCurrentSessionUseCase,
    private val updateProjectInCurrentSession: UpdateProjectInCurrentSessionUseCase,
    private val getEnrolmentCreationEventForSubject: GetEnrolmentCreationEventForSubjectUseCase,
    private val deleteSessionEventsIfNeeded: DeleteSessionEventsIfNeededUseCase,
    private val isFlowCompletedWithError: IsFlowCompletedWithErrorUseCase,
) : ViewModel() {

    private var cachedRequest: ActionRequest? = null
    private val loginAlreadyTried: AtomicBoolean = AtomicBoolean(false)

    val proceedWithAction: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _proceedWithAction
    private val _proceedWithAction = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()

    val returnResponse: LiveData<LiveDataEventWithContent<ActionResponse>>
        get() = _returnResponse
    private val _returnResponse = MutableLiveData<LiveDataEventWithContent<ActionResponse>>()

    val showAlert: LiveData<LiveDataEventWithContent<ClientApiError>>
        get() = _showAlert
    private val _showAlert = MutableLiveData<LiveDataEventWithContent<ClientApiError>>()

    val showLoginFlow: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _showLoginFlow
    private val _showLoginFlow = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()


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
            handleErrorResponse(actionRequest, ClientApiResultError(IAppErrorReason.LOGIN_NOT_COMPLETE))
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
                        handleErrorResponse(cachedRequest!!, ClientApiResultError(IAppErrorReason.LOGIN_NOT_COMPLETE))
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

    // *********************************************************************************************
    // Response handling
    // *********************************************************************************************

    // TODO review if parameters should be replaced with :feature:orchestrator models
    fun handleEnrolResponse(
        action: ActionRequest,
        enrolResponse: IAppEnrolResponse,
    ) = viewModelScope.launch {
        // need to get sessionId before it is closed and null
        val currentSessionId = clientSessionManager.getCurrentSessionId()

        clientSessionManager.addCompletionCheckEvent(flowCompleted = true)
        clientSessionManager.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        val coSyncEnrolmentRecords = getEnrolmentCreationEventForSubject(action.projectId, enrolResponse.guid)

        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.EnrolActionResponse(
            request = action,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            enrolledGuid = enrolResponse.guid,
            subjectActions = coSyncEnrolmentRecords,
        ))
    }

    // TODO review if parameters should be replaced with :feature:orchestrator models
    fun handleIdentifyResponse(
        action: ActionRequest,
        identifyResponse: IAppIdentifyResponse,
    ) = viewModelScope.launch {
        val currentSessionId = clientSessionManager.getCurrentSessionId()
        clientSessionManager.addCompletionCheckEvent(flowCompleted = true)

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)

        _returnResponse.send(ActionResponse.IdentifyActionResponse(
            request = action,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            identifications = identifyResponse.identifications,
        ))
    }

    // TODO review if parameters should be replaced with :feature:orchestrator models
    fun handleConfirmResponse(
        action: ActionRequest,
        confirmResponse: IAppConfirmationResponse,
    ) = viewModelScope.launch {
        val currentSessionId = clientSessionManager.getCurrentSessionId()
        clientSessionManager.addCompletionCheckEvent(flowCompleted = true)

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.ConfirmActionResponse(
            request = action,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            confirmed = confirmResponse.identificationOutcome,
        ))
    }

    // TODO review if parameters should be replaced with :feature:orchestrator models
    fun handleVerifyResponse(
        action: ActionRequest,
        verifyResponse: IAppVerifyResponse,
    ) = viewModelScope.launch {
        val currentSessionId = clientSessionManager.getCurrentSessionId()
        clientSessionManager.addCompletionCheckEvent(flowCompleted = true)
        clientSessionManager.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.VerifyActionResponse(
            request = action,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            matchResult = verifyResponse.matchResult,
        ))
    }

    // TODO review if parameters should be replaced with :feature:orchestrator models
    fun handleExitFormResponse(
        action: ActionRequest,
        exitFormResponse: IAppRefusalFormResponse,
    ) = viewModelScope.launch {
        val currentSessionId = clientSessionManager.getCurrentSessionId()
        clientSessionManager.addCompletionCheckEvent(flowCompleted = true)
        clientSessionManager.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.ExitFormActionResponse(
            request = action,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            reason = exitFormResponse.reason,
            extraText = exitFormResponse.extra,
        ))
    }

    // TODO review if parameters should be replaced with :feature:orchestrator models
    fun handleErrorResponse(
        action: ActionRequest,
        errorResponse: IAppErrorResponse,
    ) = viewModelScope.launch {
        val currentSessionId = clientSessionManager.getCurrentSessionId()

        val flowCompleted = isFlowCompletedWithError(errorResponse)
        clientSessionManager.addCompletionCheckEvent(flowCompleted = flowCompleted)
        clientSessionManager.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.ErrorActionResponse(
            request = action,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            reason = errorResponse.reason,
            flowCompleted = flowCompleted,
        ))
    }
}

package com.simprints.feature.clientapi.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.models.ActionResponse
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.feature.clientapi.session.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.session.GetEnrolmentCreationEventForSubjectUseCase
import com.simprints.feature.clientapi.session.GetEventJsonForSessionUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ClientApiViewModel @Inject constructor(
    private val rootManager: SecurityManager,
    private val intentMapper: IntentToActionMapper,
    private val clientSessionManager: ClientSessionManager,
    private val getEventJsonForSession: GetEventJsonForSessionUseCase,
    private val getEnrolmentCreationEventForSubject: GetEnrolmentCreationEventForSubjectUseCase,
    private val deleteSessionEventsIfNeeded: DeleteSessionEventsIfNeededUseCase,
) : ViewModel() {

    val proceedWithAction: LiveData<LiveDataEventWithContent<ActionRequest>>
        get() = _proceedWithAction
    private val _proceedWithAction = MutableLiveData<LiveDataEventWithContent<ActionRequest>>()

    val returnResponse: LiveData<LiveDataEventWithContent<ActionResponse>>
        get() = _returnResponse
    private val _returnResponse = MutableLiveData<LiveDataEventWithContent<ActionResponse>>()

    val showAlert: LiveData<LiveDataEventWithContent<ClientApiError>>
        get() = _showAlert
    private val _showAlert = MutableLiveData<LiveDataEventWithContent<ClientApiError>>()

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
        val action = intentMapper(action, extras)
        clientSessionManager.reportUnknownExtras(action.unknownExtras)

        // TODO proceed processing action
        _proceedWithAction.send(action) // TODO replace with user flow builder

    } catch (validationException: InvalidRequestException) {
        Simber.e(validationException)
        clientSessionManager.addInvalidIntentEvent(action, extras)
        _showAlert.send(validationException.error)
    }

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

        val flowCompleted = isFlowCompletedWithCurrentError(errorResponse)
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

    private fun isFlowCompletedWithCurrentError(errorResponse: IAppErrorResponse) = when (errorResponse.reason) {
        IAppErrorReason.UNEXPECTED_ERROR,
        IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN,
        IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN,
        IAppErrorReason.BLUETOOTH_NOT_SUPPORTED,
        IAppErrorReason.BLUETOOTH_NO_PERMISSION,
        IAppErrorReason.GUID_NOT_FOUND_ONLINE,
        IAppErrorReason.PROJECT_PAUSED,
        IAppErrorReason.PROJECT_ENDING,
        -> true

        IAppErrorReason.ROOTED_DEVICE,
        IAppErrorReason.ENROLMENT_LAST_BIOMETRICS_FAILED,
        IAppErrorReason.LOGIN_NOT_COMPLETE,
        IAppErrorReason.FINGERPRINT_CONFIGURATION_ERROR,
        IAppErrorReason.FACE_LICENSE_MISSING,
        IAppErrorReason.FACE_LICENSE_INVALID,
        IAppErrorReason.FACE_CONFIGURATION_ERROR,
        IAppErrorReason.BACKEND_MAINTENANCE_ERROR,
        -> false
    }

}


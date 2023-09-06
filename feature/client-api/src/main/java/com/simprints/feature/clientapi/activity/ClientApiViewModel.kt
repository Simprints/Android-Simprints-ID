package com.simprints.feature.clientapi.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.clientapi.activity.usecases.IsFlowCompletedWithErrorUseCase
import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.models.ActionResponse
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.session.ClientSessionManager
import com.simprints.feature.clientapi.session.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.session.GetEnrolmentCreationEventForSubjectUseCase
import com.simprints.feature.clientapi.session.GetEventJsonForSessionUseCase
import com.simprints.moduleapi.app.responses.IAppConfirmationResponse
import com.simprints.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppIdentifyResponse
import com.simprints.moduleapi.app.responses.IAppRefusalFormResponse
import com.simprints.moduleapi.app.responses.IAppVerifyResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ClientApiViewModel @Inject constructor(
    private val clientSessionManager: ClientSessionManager,
    private val getEventJsonForSession: GetEventJsonForSessionUseCase,
    private val getEnrolmentCreationEventForSubject: GetEnrolmentCreationEventForSubjectUseCase,
    private val deleteSessionEventsIfNeeded: DeleteSessionEventsIfNeededUseCase,
    private val isFlowCompletedWithError: IsFlowCompletedWithErrorUseCase,
) : ViewModel() {

    val returnResponse: LiveData<LiveDataEventWithContent<ActionResponse>>
        get() = _returnResponse
    private val _returnResponse = MutableLiveData<LiveDataEventWithContent<ActionResponse>>()

    val showAlert: LiveData<LiveDataEventWithContent<ClientApiError>>
        get() = _showAlert
    private val _showAlert = MutableLiveData<LiveDataEventWithContent<ClientApiError>>()


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

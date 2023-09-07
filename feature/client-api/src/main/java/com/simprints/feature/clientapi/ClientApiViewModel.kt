package com.simprints.feature.clientapi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.usecases.CreateSessionIfRequiredUseCase
import com.simprints.feature.clientapi.usecases.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.usecases.GetCurrentSessionIdUseCase
import com.simprints.feature.clientapi.usecases.GetEnrolmentCreationEventForSubjectUseCase
import com.simprints.feature.clientapi.usecases.GetEventJsonForSessionUseCase
import com.simprints.feature.clientapi.usecases.IsFlowCompletedWithErrorUseCase
import com.simprints.feature.clientapi.usecases.SimpleEventReporter
import com.simprints.feature.orchestrator.models.ActionRequest
import com.simprints.feature.orchestrator.models.ActionRequestIdentifier
import com.simprints.feature.orchestrator.models.ActionResponse
import com.simprints.infra.logging.Simber
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
    private val simpleEventReporter: SimpleEventReporter,
    private val getCurrentSessionId: GetCurrentSessionIdUseCase,
    private val intentMapper: IntentToActionMapper,
    private val createSessionIfRequiredUseCase: CreateSessionIfRequiredUseCase,
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


    suspend fun handleIntent(action: String, extras: Map<String, Any>): ActionRequest? = try {
        // Session must be created to be able to report invalid intents if mapping fails
        createSessionIfRequiredUseCase(action)
        intentMapper(action, extras)
    } catch (validationException: InvalidRequestException) {
        Simber.e(validationException)
        simpleEventReporter.addInvalidIntentEvent(action, extras)
        _showAlert.send(validationException.error)
        null
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
        val currentSessionId = getCurrentSessionId()

        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)
        simpleEventReporter.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        val coSyncEnrolmentRecords = getEnrolmentCreationEventForSubject(action.projectId, enrolResponse.guid)

        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.EnrolActionResponse(
            actionIdentifier = action.actionIdentifier,
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
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)

        _returnResponse.send(ActionResponse.IdentifyActionResponse(
            actionIdentifier = action.actionIdentifier,
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
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.ConfirmActionResponse(
            actionIdentifier = action.actionIdentifier,
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
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)
        simpleEventReporter.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.VerifyActionResponse(
            actionIdentifier = action.actionIdentifier,
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
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)
        simpleEventReporter.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.ExitFormActionResponse(
            actionIdentifier = action.actionIdentifier,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            reason = exitFormResponse.reason,
            extraText = exitFormResponse.extra,
        ))
    }

    // TODO review if parameters should be replaced with :feature:orchestrator models
    // Error is a special case where it might be called before action has been parsed,
    // therefore it can only rely on the identifier from action string to be present
    fun handleErrorResponse(
        actionIdentifier: ActionRequestIdentifier,
        errorResponse: IAppErrorResponse,
    ) = viewModelScope.launch {
        val currentSessionId = getCurrentSessionId()

        val flowCompleted = isFlowCompletedWithError(errorResponse)
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = flowCompleted)
        simpleEventReporter.closeCurrentSessionNormally()

        val coSyncEventsJson = getEventJsonForSession(currentSessionId)
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(ActionResponse.ErrorActionResponse(
            actionIdentifier = actionIdentifier,
            sessionId = currentSessionId,
            eventsJson = coSyncEventsJson,
            reason = errorResponse.reason,
            flowCompleted = flowCompleted,
        ))
    }
}

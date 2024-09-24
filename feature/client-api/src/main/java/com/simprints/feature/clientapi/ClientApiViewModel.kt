package com.simprints.feature.clientapi

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.clientapi.exceptions.InvalidRequestException
import com.simprints.feature.clientapi.extensions.toMap
import com.simprints.feature.clientapi.mappers.request.IntentToActionMapper
import com.simprints.feature.clientapi.mappers.response.ActionToIntentMapper
import com.simprints.feature.clientapi.models.ClientApiError
import com.simprints.feature.clientapi.usecases.CreateSessionIfRequiredUseCase
import com.simprints.feature.clientapi.usecases.DeleteSessionEventsIfNeededUseCase
import com.simprints.feature.clientapi.usecases.GetCurrentSessionIdUseCase
import com.simprints.feature.clientapi.usecases.GetEnrolmentCreationEventForSubjectUseCase
import com.simprints.feature.clientapi.usecases.IsFlowCompletedWithErrorUseCase
import com.simprints.feature.clientapi.usecases.SimpleEventReporter
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier
import com.simprints.infra.orchestration.data.ActionResponse
import com.simprints.infra.orchestration.data.responses.AppConfirmationResponse
import com.simprints.infra.orchestration.data.responses.AppEnrolResponse
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import com.simprints.infra.orchestration.data.responses.AppVerifyResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientApiViewModel @Inject internal constructor(
    private val intentMapper: IntentToActionMapper,
    private val resultMapper: ActionToIntentMapper,
    private val simpleEventReporter: SimpleEventReporter,
    private val getCurrentSessionId: GetCurrentSessionIdUseCase,
    private val createSessionIfRequiredUseCase: CreateSessionIfRequiredUseCase,
    private val getEnrolmentCreationEventForSubject: GetEnrolmentCreationEventForSubjectUseCase,
    private val deleteSessionEventsIfNeeded: DeleteSessionEventsIfNeededUseCase,
    private val isFlowCompletedWithError: IsFlowCompletedWithErrorUseCase,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
) : ViewModel() {

    val returnResponse: LiveData<LiveDataEventWithContent<Bundle>>
        get() = _returnResponse
    private val _returnResponse = MutableLiveData<LiveDataEventWithContent<Bundle>>()

    val newSessionCreated: LiveData<LiveDataEvent>
        get() = _newSessionCreated
    private val _newSessionCreated = MutableLiveData<LiveDataEvent>()

    val showAlert: LiveData<LiveDataEventWithContent<ClientApiError>>
        get() = _showAlert
    private val _showAlert = MutableLiveData<LiveDataEventWithContent<ClientApiError>>()

    private suspend fun getProject() =
        runCatching { configManager.getProject(authStore.signedInProjectId) }.getOrNull()

    suspend fun handleIntent(action: String, extras: Bundle): ActionRequest? {
        val extrasMap = extras.toMap()
        return try {
            // Session must be created to be able to report invalid intents if mapping fails
            if (createSessionIfRequiredUseCase(action)) {
                _newSessionCreated.send()
            }
            intentMapper(action = action, extras = extrasMap, project = getProject())
        } catch (validationException: InvalidRequestException) {
            Simber.e(validationException)
            simpleEventReporter.addInvalidIntentEvent(action, extrasMap)
            _showAlert.send(validationException.error)
            null
        }
    }

    // *********************************************************************************************
    // Response handling
    // *********************************************************************************************

    fun handleEnrolResponse(
        action: ActionRequest,
        enrolResponse: AppEnrolResponse,
    ) = viewModelScope.launch {
        // need to get sessionId before it is closed and null
        val currentSessionId = getCurrentSessionId()

        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)
        simpleEventReporter.closeCurrentSessionNormally()

        val coSyncEnrolmentRecords =
            getEnrolmentCreationEventForSubject(action.projectId, enrolResponse.guid)

        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(
            resultMapper(
                ActionResponse.EnrolActionResponse(
                    actionIdentifier = action.actionIdentifier,
                    sessionId = currentSessionId,
                    enrolledGuid = enrolResponse.guid,
                    subjectActions = coSyncEnrolmentRecords,
                )
            )
        )
    }

    fun handleIdentifyResponse(
        action: ActionRequest,
        identifyResponse: AppIdentifyResponse,
    ) = viewModelScope.launch {
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)

        _returnResponse.send(
            resultMapper(
                ActionResponse.IdentifyActionResponse(
                    actionIdentifier = action.actionIdentifier,
                    sessionId = currentSessionId,
                    identifications = identifyResponse.identifications,
                )
            )
        )
    }

    fun handleConfirmResponse(
        action: ActionRequest,
        confirmResponse: AppConfirmationResponse,
    ) = viewModelScope.launch {
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)

        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(
            resultMapper(
                ActionResponse.ConfirmActionResponse(
                    actionIdentifier = action.actionIdentifier,
                    sessionId = currentSessionId,
                    confirmed = confirmResponse.identificationOutcome,
                )
            )
        )
    }

    fun handleVerifyResponse(
        action: ActionRequest,
        verifyResponse: AppVerifyResponse,
    ) = viewModelScope.launch {
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)
        simpleEventReporter.closeCurrentSessionNormally()

        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(
            resultMapper(
                ActionResponse.VerifyActionResponse(
                    actionIdentifier = action.actionIdentifier,
                    sessionId = currentSessionId,
                    matchResult = verifyResponse.matchResult,
                )
            )
        )
    }

    fun handleExitFormResponse(
        action: ActionRequest,
        exitFormResponse: AppRefusalResponse,
    ) = viewModelScope.launch {
        val currentSessionId = getCurrentSessionId()
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = true)
        simpleEventReporter.closeCurrentSessionNormally()

        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(
            resultMapper(
                ActionResponse.ExitFormActionResponse(
                    actionIdentifier = action.actionIdentifier,
                    sessionId = currentSessionId,
                    reason = exitFormResponse.reason,
                    extraText = exitFormResponse.extra,
                )
            )
        )
    }

    // Error is a special case where it might be called before action has been parsed,
    // therefore it can only rely on the identifier from action string to be present
    fun handleErrorResponse(
        action: String,
        errorResponse: AppErrorResponse,
    ) = viewModelScope.launch {
        val currentSessionId = getCurrentSessionId()

        val flowCompleted = isFlowCompletedWithError(errorResponse)
        simpleEventReporter.addCompletionCheckEvent(flowCompleted = flowCompleted)
        simpleEventReporter.closeCurrentSessionNormally()
        deleteSessionEventsIfNeeded(currentSessionId)

        _returnResponse.send(
            resultMapper(
                ActionResponse.ErrorActionResponse(
                    actionIdentifier = ActionRequestIdentifier.fromIntentAction(action),
                    sessionId = currentSessionId,
                    reason = errorResponse.reason,
                    flowCompleted = flowCompleted,
                )
            )
        )
    }

}

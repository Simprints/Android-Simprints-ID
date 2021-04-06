package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.Constants.RETURN_FOR_FLOW_COMPLETED
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.commcare.CommCareAction.*
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.exceptions.CouldntSaveEventException
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.domain.modality.toMode
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

private const val RETURN_TIMEOUT = 1_000L

class CommCarePresenter(
    private val view: CommCareContract.View,
    private val action: CommCareAction,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val jsonHelper: JsonHelper,
    private val subjectRepository: SubjectRepository,
    private val timeHelper: ClientApiTimeHelper,
    deviceManager: DeviceManager,
    crashReportManager: ClientApiCrashReportManager
) : RequestPresenter(
    view,
    sessionEventsManager,
    deviceManager,
    crashReportManager
), CommCareContract.Presenter {

    override suspend fun start() {
        if (action !is CommCareActionFollowUpAction) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.COMMCARE)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        runIfDeviceIsNotRooted {
            when (action) {
                Enrol -> processEnrolRequest()
                Identify -> processIdentifyRequest()
                Verify -> processVerifyRequest()
                ConfirmIdentity -> checkAndProcessSessionId()
                Invalid -> throw InvalidIntentActionException()
            }.safeSealedWhens
        }
    }

    override fun handleEnrolResponse(enrol: EnrolResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnRegistration(
                enrol.guid,
                currentSessionId,
                flowCompletedCheck,
                getEventsJsonForSession(currentSessionId),
                getEnrolmentCreationEventForSubject(enrol.guid)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    /**
     * CommCare processes Identifications results as LibSimprints format.
     *
     * CommCare doesn't seem to handle flowCompletedCheck, so it shouldn't matter if we send it back or not.
     *
     * Identification doesn't delete session events because we need them for the confirmation return.
     */
    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        sharedPreferencesManager.stashSessionId(identify.sessionId)

        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnIdentification(ArrayList(identify.identifications.map {
                Identification(it.guidFound, it.confidenceScore, Tier.valueOf(it.tier.name))
            }), identify.sessionId, getEventsJsonForSession(identify.sessionId))
        }
    }

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnConfirmation(
                flowCompletedCheck,
                currentSessionId,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnVerification(
                verify.matchResult.confidenceScore,
                Tier.valueOf(verify.matchResult.tier.name),
                verify.matchResult.guidFound,
                currentSessionId,
                flowCompletedCheck,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnExitForms(
                refusalForm.reason,
                refusalForm.extra,
                currentSessionId,
                flowCompletedCheck,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnErrorToClient(
                errorResponse,
                flowCompletedCheck,
                currentSessionId,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    /**
     * Be aware that Android Intents have a cap at around 500KB of data that can be returned.
     * When changing events, make sure they still fit in.
     */
    private suspend fun getEventsJsonForSession(sessionId: String): String? =
        if (sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.COMMCARE)) {
            val events = sessionEventsManager.getAllEventsForSession(sessionId).toList()
            jsonHelper.toJson(CommCareEvents(events))
        } else {
            null
        }

    private suspend fun getEnrolmentCreationEventForSubject(subjectId: String): String? {
        if (!sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.COMMCARE)) return null

        val recordCreationEvent =
            subjectRepository.load(SubjectQuery(projectId = getProjectIdFromRequest(), subjectId = subjectId))
                .firstOrNull()
                ?.fromSubjectToEnrolmentCreationEvent()
                ?: return null

        return jsonHelper.toJson(CommCareEvents(listOf(recordCreationEvent)))
    }

    /**
     * This method will wait [RETURN_TIMEOUT] for the completion event to finish.
     * If it doesn't finish, it will log an error and continue the flow.
     *
     * We should not cause problems to the calling app if an event can't be saved at the end of the flow.
     */
    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) {
        val job = withTimeoutOrNull(RETURN_TIMEOUT) {
            sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)
        }

        if (job == null) {
            Timber.e(CouldntSaveEventException("Event: FlowCompletedEvent"))
            crashReportManager.logExceptionOrSafeException(CouldntSaveEventException("Event: FlowCompletedEvent"))
        }
    }

    private suspend fun checkAndProcessSessionId() {
        if ((view.extras?.get(Constants.SIMPRINTS_SESSION_ID) as CharSequence?).isNullOrBlank()) {
            if (sharedPreferencesManager.peekSessionId().isNotBlank()) {
                view.injectSessionIdIntoIntent(sharedPreferencesManager.popSessionId())
            }
        }

        processConfirmIdentityRequest()
    }

    /**
     * Delete the events if returning to CommCare but not Simprints
     */
    private suspend fun deleteSessionEventsIfNeeded(sessionId: String) {
        if (sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.COMMCARE) &&
            !sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.SIMPRINTS)
        ) {
            sessionEventsManager.deleteSessionEvents(sessionId)
        }
    }

    private fun getProjectIdFromRequest() = view.extras?.get(Constants.SIMPRINTS_PROJECT_ID) as String

    private fun Subject.fromSubjectToEnrolmentCreationEvent(): EnrolmentRecordCreationEvent {
        return EnrolmentRecordCreationEvent(
            timeHelper.now(),
            subjectId,
            projectId,
            moduleId,
            attendantId,
            sharedPreferencesManager.modalities.map { it.toMode() },
            EnrolmentRecordCreationEvent.buildBiometricReferences(fingerprintSamples, faceSamples)
        )
    }

    private data class CommCareEvents(val events: List<Event>)
}

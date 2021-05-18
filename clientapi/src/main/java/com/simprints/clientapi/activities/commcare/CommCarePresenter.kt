package com.simprints.clientapi.activities.commcare

import androidx.annotation.Keep
import com.simprints.clientapi.Constants.RETURN_FOR_FLOW_COMPLETED
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.commcare.CommCareAction.*
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
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

class CommCarePresenter(
    private val view: CommCareContract.View,
    private val action: CommCareAction,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val jsonHelper: JsonHelper,
    private val subjectRepository: SubjectRepository,
    private val timeHelper: ClientApiTimeHelper,
    deviceManager: DeviceManager,
    crashReportManager: ClientApiCrashReportManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
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
        coroutineScope.launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            sessionEventsManager.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnRegistration(
                enrol.guid,
                currentSessionId,
                RETURN_FOR_FLOW_COMPLETED,
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

        coroutineScope.launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)
            view.returnIdentification(ArrayList(identify.identifications.map {
                Identification(it.guidFound, it.confidenceScore, Tier.valueOf(it.tier.name))
            }), identify.sessionId, getEventsJsonForSession(identify.sessionId))
        }
    }

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        coroutineScope.launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            sessionEventsManager.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnConfirmation(
                RETURN_FOR_FLOW_COMPLETED,
                currentSessionId,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        coroutineScope.launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            sessionEventsManager.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnVerification(
                verify.matchResult.confidenceScore,
                Tier.valueOf(verify.matchResult.tier.name),
                verify.matchResult.guidFound,
                currentSessionId,
                RETURN_FOR_FLOW_COMPLETED,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        coroutineScope.launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            sessionEventsManager.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnExitForms(
                refusalForm.reason,
                refusalForm.extra,
                currentSessionId,
                RETURN_FOR_FLOW_COMPLETED,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        coroutineScope.launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)
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
            subjectRepository.load(
                SubjectQuery(
                    projectId = getProjectIdFromRequest(),
                    subjectId = subjectId
                )
            )
                .firstOrNull()
                ?.fromSubjectToEnrolmentCreationEvent()
                ?: return null

        return jsonHelper.toJson(CommCareEvents(listOf(recordCreationEvent)))
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

    private fun getProjectIdFromRequest() =
        view.extras?.get(Constants.SIMPRINTS_PROJECT_ID) as String

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

    @Keep
    private data class CommCareEvents(val events: List<Event>)
}

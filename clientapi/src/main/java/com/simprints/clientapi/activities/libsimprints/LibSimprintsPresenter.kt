package com.simprints.clientapi.activities.libsimprints

import androidx.annotation.Keep
import com.simprints.clientapi.Constants
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.Enrol
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.Identify
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.Invalid
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.EnrolLastBiometrics
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.Verify
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.ConfirmationResponse
import com.simprints.clientapi.domain.responses.EnrolResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.core.domain.modality.toMode
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import com.simprints.libsimprints.Constants as LibSimprintsConstants

class LibSimprintsPresenter(
    private val view: LibSimprintsContract.View,
    private val action: LibSimprintsAction,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager,
    private val timeHelper: ClientApiTimeHelper,
    crashReportManager: ClientApiCrashReportManager,
    private val subjectRepository: SubjectRepository,
    private val jsonHelper: JsonHelper,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val encoder: EncodingUtils = EncodingUtilsImpl
) : RequestPresenter(
    view,
    sessionEventsManager,
    deviceManager,
    crashReportManager
), LibSimprintsContract.Presenter {

    override suspend fun start() {
        if (action !is LibSimprintsActionFollowUpAction) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.STANDARD)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        runIfDeviceIsNotRooted {
            when (action) {
                Enrol -> processEnrolRequest()
                Identify -> processIdentifyRequest()
                Verify -> processVerifyRequest()
                ConfirmIdentity -> processConfirmIdentityRequest()
                EnrolLastBiometrics -> processEnrolLastBiometrics()
                Invalid -> throw InvalidIntentActionException()
            }.safeSealedWhens
        }
    }

    override fun handleEnrolResponse(enrol: EnrolResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnRegistration(
                Registration(enrol.guid), currentSessionId, flowCompletedCheck,
                getEventsJsonForSession(currentSessionId),
                getEnrolmentCreationEventForSubject(enrol.guid)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnIdentification(
                ArrayList(identify.identifications.map {
                    Identification(
                        it.guidFound,
                        it.confidenceScore,
                        it.tier.fromDomainToLibsimprintsTier()
                    )
                }), identify.sessionId, flowCompletedCheck,
                getEventsJsonForSession(identify.sessionId)
            )
        }
    }


    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnConfirmation(
                flowCompletedCheck, currentSessionId,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            with(verify) {
                val verification = Verification(
                    matchResult.confidenceScore,
                    matchResult.tier.fromDomainToLibsimprintsTier(),
                    matchResult.guidFound
                )
                view.returnVerification(
                    verification, currentSessionId, flowCompletedCheck,
                    getEventsJsonForSession(currentSessionId)
                )
            }

            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnRefusalForms(
                RefusalForm(refusalForm.reason, refusalForm.extra),
                currentSessionId, flowCompletedCheck,
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
                errorResponse, flowCompletedCheck, currentSessionId,
                getEventsJsonForSession(currentSessionId)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
        }
    }

    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)

    private suspend fun getEventsJsonForSession(sessionId: String): String? =
        if (sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.COSYNC)) {
            val events = sessionEventsManager.getAllEventsForSession(sessionId).toList()
            jsonHelper.toJson(GenericCoSyncEvents(events))
        } else {
            null
        }

    private suspend fun getEnrolmentCreationEventForSubject(subjectId: String): String? {
        if (!sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.COSYNC)) return null

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

        return jsonHelper.toJson(GenericCoSyncEvents(listOf(recordCreationEvent)))
    }

    private fun getProjectIdFromRequest() =
        view.extras?.get(LibSimprintsConstants.SIMPRINTS_PROJECT_ID) as String

    private fun Subject.fromSubjectToEnrolmentCreationEvent(): EnrolmentRecordCreationEvent {
        return EnrolmentRecordCreationEvent(
            timeHelper.now(),
            subjectId,
            projectId,
            moduleId,
            attendantId,
            sharedPreferencesManager.modalities.map { it.toMode() },
            EnrolmentRecordCreationEvent.buildBiometricReferences(
                fingerprintSamples,
                faceSamples,
                encoder
            )
        )
    }

    /**
     * Delete the events if returning to a cosync project but not Simprints
     */
    private suspend fun deleteSessionEventsIfNeeded(sessionId: String) {
        if (sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.COSYNC) &&
            !sharedPreferencesManager.syncDestinationSettings.contains(SyncDestinationSetting.SIMPRINTS)
        ) {
            sessionEventsManager.deleteSessionEvents(sessionId)
        }
    }

    @Keep
    private data class GenericCoSyncEvents(val events: List<Event>)
}


package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.Constants.RETURN_FOR_FLOW_COMPLETED
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.commcare.CommCareAction.Enrol
import com.simprints.clientapi.activities.commcare.CommCareAction.Identify
import com.simprints.clientapi.activities.commcare.CommCareAction.Invalid
import com.simprints.clientapi.activities.commcare.CommCareAction.Verify
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
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    view = view,
    eventsManager = sessionEventsManager,
    deviceManager = deviceManager,
    crashReportManager = crashReportManager,
    sharedPreferencesManager = sharedPreferencesManager,
    sessionEventsManager = sessionEventsManager
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
                getEventsJsonForSession(currentSessionId, jsonHelper),
                getEnrolmentCreationEventForSubject(
                    enrol.guid,
                    subjectRepository = subjectRepository,
                    timeHelper = timeHelper,
                    jsonHelper = jsonHelper
                )
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
            }), identify.sessionId, getEventsJsonForSession(identify.sessionId, jsonHelper))
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
                getEventsJsonForSession(currentSessionId, jsonHelper)
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
                getEventsJsonForSession(currentSessionId, jsonHelper)
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
                getEventsJsonForSession(currentSessionId, jsonHelper)
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
                getEventsJsonForSession(currentSessionId, jsonHelper)
            )
            deleteSessionEventsIfNeeded(currentSessionId)
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
}

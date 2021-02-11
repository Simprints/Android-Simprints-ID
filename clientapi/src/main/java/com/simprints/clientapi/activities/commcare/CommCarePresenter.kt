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
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class CommCarePresenter(
    private val view: CommCareContract.View,
    private val action: CommCareAction,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    deviceManager: DeviceManager,
    crashReportManager: ClientApiCrashReportManager,
    private val jsonHelper: JsonHelper
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
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnRegistration(
                enrol.guid,
                getCurrentSessionIdOrEmpty(),
                flowCompletedCheck,
                getEventsJsonForSession(getCurrentSessionIdOrEmpty())
            )
            deleteSessionEventsIfNeeded(getCurrentSessionIdOrEmpty())
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
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnConfirmation(
                flowCompletedCheck,
                getCurrentSessionIdOrEmpty(),
                getEventsJsonForSession(getCurrentSessionIdOrEmpty())
            )
            deleteSessionEventsIfNeeded(getCurrentSessionIdOrEmpty())
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnErrorToClient(
                errorResponse,
                flowCompletedCheck,
                getCurrentSessionIdOrEmpty(),
                getEventsJsonForSession(getCurrentSessionIdOrEmpty())
            )
            deleteSessionEventsIfNeeded(getCurrentSessionIdOrEmpty())
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnVerification(
                verify.matchResult.confidenceScore,
                Tier.valueOf(verify.matchResult.tier.name),
                verify.matchResult.guidFound,
                getCurrentSessionIdOrEmpty(),
                flowCompletedCheck,
                getEventsJsonForSession(getCurrentSessionIdOrEmpty())
            )
            deleteSessionEventsIfNeeded(getCurrentSessionIdOrEmpty())
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnExitForms(
                refusalForm.reason,
                refusalForm.extra,
                getCurrentSessionIdOrEmpty(),
                flowCompletedCheck,
                getEventsJsonForSession(getCurrentSessionIdOrEmpty())
            )
            deleteSessionEventsIfNeeded(getCurrentSessionIdOrEmpty())
        }
    }

    /**
     * Be aware that Android Intents have a cap at around 500KB of data that can be returned.
     * When changing events, make sure they still fit in.
     */
    private suspend fun getEventsJsonForSession(sessionId: String): String? =
        if (sharedPreferencesManager.syncDestinationSetting == SyncDestinationSetting.COMMCARE) {
            val events = sessionEventsManager.getAllEventsForSession(sessionId).toList()
            jsonHelper.toJson(CommCareEvents(events))
        } else {
            null
        }

    private suspend fun getCurrentSessionIdOrEmpty() = sessionEventsManager.getCurrentSessionId()

    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)

    private suspend fun checkAndProcessSessionId() {
        if ((view.extras?.get(Constants.SIMPRINTS_SESSION_ID) as CharSequence?).isNullOrBlank()) {
            if (sharedPreferencesManager.peekSessionId().isNotBlank()) {
                view.injectSessionIdIntoIntent(sharedPreferencesManager.popSessionId())
            }
        }

        processConfirmIdentityRequest()
    }

    private suspend fun deleteSessionEventsIfNeeded(sessionId: String) {
        if (sharedPreferencesManager.syncDestinationSetting == SyncDestinationSetting.COMMCARE) {
            sessionEventsManager.deleteSessionEvents(sessionId)
        }
    }

    private data class CommCareEvents(val events: List<Event>)
}

package com.simprints.clientapi.activities.libsimprints

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
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibSimprintsPresenter(
    private val view: LibSimprintsContract.View,
    private val action: LibSimprintsAction,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager,
    private val timeHelper: ClientApiTimeHelper,
    crashReportManager: ClientApiCrashReportManager,
    private val subjectRepository: SubjectRepository,
    private val jsonHelper: JsonHelper,
    private val sharedPreferencesManager: SharedPreferencesManager
) : RequestPresenter(
    view = view,
    eventsManager = sessionEventsManager,
    deviceManager = deviceManager,
    crashReportManager = crashReportManager
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
                getSessionEventsJson(currentSessionId),
                getEnrolmentCreationEventForSubject(
                    enrol.guid,
                    sharedPreferencesManager = sharedPreferencesManager,
                    subjectRepository = subjectRepository,
                    timeHelper = timeHelper,
                    jsonHelper = jsonHelper
                )
            )
            deleteSessionEventsIfRequired(currentSessionId)
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
                getSessionEventsJson(identify.sessionId)
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
                getSessionEventsJson(currentSessionId)
            )
            deleteSessionEventsIfRequired(currentSessionId)
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
                    getSessionEventsJson(currentSessionId)
                )
            }

            deleteSessionEventsIfRequired(currentSessionId)
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
                getSessionEventsJson(currentSessionId)
            )
            deleteSessionEventsIfRequired(currentSessionId)
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
                getSessionEventsJson(currentSessionId)
            )
            deleteSessionEventsIfRequired(currentSessionId)
        }
    }

    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)

    /*These two methods are just convenience methods to avoid calling methods with long argument lists
    * multiple times*/
    private suspend fun deleteSessionEventsIfRequired(sessionId: String) =
        deleteSessionEventsIfNeeded(
            sessionId = sessionId,
            sharedPreferencesManager = sharedPreferencesManager,
            sessionEventsManager = sessionEventsManager
        )

    private suspend fun getSessionEventsJson(sessionId: String) = getEventsJsonForSession(
        sessionId = sessionId,
        sharedPreferencesManager = sharedPreferencesManager,
        sessionEventsManager = sessionEventsManager,
        jsonHelper = jsonHelper
    )
}


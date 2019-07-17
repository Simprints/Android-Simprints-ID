package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier


class LibSimprintsPresenter(private val view: LibSimprintsContract.View,
                            private val action: String?,
                            private val sessionEventsManager: ClientApiSessionEventsManager,
                            private val crashReportManager: ClientApiCrashReportManager) :
    RequestPresenter(view, sessionEventsManager), LibSimprintsContract.Presenter {

    override suspend fun start() {
        if(action != SIMPRINTS_SELECT_GUID_INTENT) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.STANDARD)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        when (action) {
            SIMPRINTS_REGISTER_INTENT -> processEnrollRequest()
            SIMPRINTS_IDENTIFY_INTENT -> processIdentifyRequest()
            SIMPRINTS_VERIFY_INTENT -> processVerifyRequest()
            SIMPRINTS_SELECT_GUID_INTENT -> processConfirmIdentifyRequest()
            else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        view.returnErrorToClient(errorResponse)
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) =
        view.returnRegistration(Registration(enroll.guid))

    override fun handleIdentifyResponse(identify: IdentifyResponse) =
        view.returnIdentification(ArrayList(identify.identifications.map {
            Identification(it.guidFound, it.confidence, Tier.valueOf(it.tier.name))
        }), identify.sessionId)

    override fun handleVerifyResponse(verify: VerifyResponse) = view.returnVerification(
        verify.matchResult.confidence,
        Tier.valueOf(verify.matchResult.tier.name),
        verify.matchResult.guidFound
    )

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) =
        view.returnRefusalForms(RefusalForm(refusalForm.reason, refusalForm.extra))

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        view.returnConfirmation(response.identificationOutcome)
    }

}


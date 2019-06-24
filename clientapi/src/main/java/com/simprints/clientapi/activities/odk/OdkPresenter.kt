package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.*
import com.simprints.clientapi.extensions.getConfidencesString
import com.simprints.clientapi.extensions.getIdsString
import com.simprints.clientapi.extensions.getTiersString

class OdkPresenter(private val view: OdkContract.View,
                   private val action: String?,
                   private val sessionEventsManager: ClientApiSessionEventsManager,
                   private val crashReportManager: ClientApiCrashReportManager)
    : RequestPresenter(view, sessionEventsManager), OdkContract.Presenter {

    companion object {
        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    }

    override suspend fun start() {
        if (action != ACTION_CONFIRM_IDENTITY) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.ODK)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        when (action) {
            ACTION_REGISTER -> processEnrollRequest()
            ACTION_IDENTIFY -> processIdentifyRequest()
            ACTION_VERIFY -> processVerifyRequest()
            ACTION_CONFIRM_IDENTITY -> processConfirmIdentifyRequest()
            else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        view.returnErrorToClient(errorResponse)
    }

    override fun isSimprintsSkipped(errorResponse: ErrorResponse): Boolean =
        when (errorResponse.reason) {
            UNEXPECTED_ERROR,
            DIFFERENT_PROJECT_ID_SIGNED_IN,
            DIFFERENT_USER_ID_SIGNED_IN,
            INVALID_CLIENT_REQUEST,
            INVALID_METADATA,
            INVALID_MODULE_ID,
            INVALID_PROJECT_ID,
            INVALID_SELECTED_ID,
            INVALID_SESSION_ID,
            INVALID_USER_ID,
            INVALID_VERIFY_ID -> true
            else -> false
        }

    override fun handleEnrollResponse(enroll: EnrollResponse) = view.returnRegistration(enroll.guid)

    override fun handleIdentifyResponse(identify: IdentifyResponse) = view.returnIdentification(
        identify.identifications.getIdsString(),
        identify.identifications.getConfidencesString(),
        identify.identifications.getTiersString(),
        identify.sessionId
    )

    override fun handleVerifyResponse(verify: VerifyResponse) = view.returnVerification(
        verify.matchResult.guidFound,
        verify.matchResult.confidence.toString(),
        verify.matchResult.tier.toString()
    )

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) =
        view.returnRefusalForm(refusalForm.reason, refusalForm.extra)
}

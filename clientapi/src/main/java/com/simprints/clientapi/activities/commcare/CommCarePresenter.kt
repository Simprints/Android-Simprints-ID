package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier


class CommCarePresenter(private val view: CommCareContract.View,
                        private val action: String?,
                        private val sessionEventsManager: ClientApiSessionEventsManager,
                        private val crashReportManager: ClientApiCrashReportManager)
    : RequestPresenter(view, sessionEventsManager), CommCareContract.Presenter {

    companion object {
        private const val PACKAGE_NAME = "com.simprints.commcare"
        const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    }

    override val domainErrorToCallingAppResultCode: Map<ErrorResponse.Reason, Int>
        get() = emptyMap()

    override suspend fun start() {
        val sessionId = sessionEventsManager.createSession(IntegrationInfo.STANDARD)
        crashReportManager.setSessionIdCrashlyticsKey(sessionId)

        when (action) {
            ACTION_REGISTER -> processEnrollRequest()
            ACTION_IDENTIFY -> processIdentifyRequest()
            ACTION_VERIFY -> processVerifyRequest()
            ACTION_CONFIRM_IDENTITY -> processConfirmIdentifyRequest()
            else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }
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

}

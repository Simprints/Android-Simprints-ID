package com.simprints.clientapi.activities.libsimprints

import android.annotation.SuppressLint
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.*
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import io.reactivex.rxkotlin.subscribeBy


class LibSimprintsPresenter(private val view: LibSimprintsContract.View,
                            private val action: String?,
                            private val sessionEventsManager: ClientApiSessionEventsManager,
                            private val clientApiCrashReportManager: ClientApiCrashReportManager) :
    RequestPresenter(view,
        sessionEventsManager
    ), LibSimprintsContract.Presenter {

    override val domainErrorToCallingAppResultCode: Map<ErrorResponse.Reason, Int>
        get() = mapOf(
            DIFFERENT_PROJECT_ID_SIGNED_IN to SIMPRINTS_INVALID_PROJECT_ID,
            DIFFERENT_PROJECT_ID_SIGNED_IN to SIMPRINTS_INVALID_PROJECT_ID,
            DIFFERENT_USER_ID_SIGNED_IN to SIMPRINTS_INVALID_USER_ID,
            GUID_NOT_FOUND_ONLINE to SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE,
            GUID_NOT_FOUND_OFFLINE to SIMPRINTS_VERIFY_GUID_NOT_FOUND_OFFLINE,
            UNEXPECTED_ERROR to SIMPRINTS_CANCELLED, //TODO: should we update LibSimpprints with the missing errors?,
            BLUETOOTH_NOT_SUPPORTED to SIMPRINTS_CANCELLED,
            SCANNER_LOW_BATTERY to SIMPRINTS_CANCELLED,
            UNKNOWN_BLUETOOTH_ISSUE to SIMPRINTS_CANCELLED,
            INVALID_CLIENT_REQUEST to SIMPRINTS_INVALID_INTENT_ACTION,
            INVALID_METADATA to SIMPRINTS_INVALID_METADATA,
            INVALID_MODULE_ID to SIMPRINTS_INVALID_MODULE_ID,
            INVALID_PROJECT_ID to SIMPRINTS_INVALID_PROJECT_ID,
            INVALID_SELECTED_ID to SIMPRINTS_CANCELLED,
            INVALID_SESSION_ID to SIMPRINTS_CANCELLED,
            INVALID_USER_ID to SIMPRINTS_INVALID_USER_ID,
            INVALID_VERIFY_ID to SIMPRINTS_INVALID_VERIFY_GUID)


    @SuppressLint("CheckResult")
    override fun start() {
        sessionEventsManager
            .createSession(IntegrationInfo.ODK)
            .doFinally {
                when (action) {
                    SIMPRINTS_REGISTER_INTENT -> processEnrollRequest()
                    SIMPRINTS_IDENTIFY_INTENT -> processIdentifyRequest()
                    SIMPRINTS_VERIFY_INTENT -> processVerifyRequest()
                    SIMPRINTS_SELECT_GUID_INTENT -> processConfirmIdentifyRequest()
                    else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
                }
            }.subscribeBy(
                onSuccess = { clientApiCrashReportManager.setSessionIdCrashlyticsKey(it) },
                onError = {
                    it.printStackTrace()
                })
    }


    override fun handleEnrollResponse(enroll: EnrollResponse) =
        view.returnRegistration(Registration(enroll.guid))

    override fun handleIdentifyResponse(identify: IdentifyResponse) =
        view.returnIdentification(ArrayList(identify.identifications.map {
            Identification(it.guidFound, it.confidence, Tier.valueOf(it.tier.name))
        }), identify.sessionId)

    override fun handleVerifyResponse(verify: VerifyResponse) = view.returnVerification(
        verify.matchResult.confidence, Tier.valueOf(verify.matchResult.tier.name), verify.matchResult.guidFound
    )

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) =
        view.returnRefusalForms(RefusalForm(refusalForm.reason, refusalForm.extra))

}


package com.simprints.clientapi.activities.libsimprints

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.json.GsonBuilder
import com.simprints.libsimprints.*
import com.simprints.libsimprints.Constants.*
import io.reactivex.rxkotlin.subscribeBy
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.*

class LibSimprintsPresenter(private val view: LibSimprintsContract.View,
                            private val action: String?,
                            private val clientApiSessionEventsManager: ClientApiSessionEventsManager,
                            private val clientApiCrashReportManager: ClientApiCrashReportManager,
                            clientApiTimeHelper: ClientApiTimeHelper,
                            gsonBuilder: GsonBuilder,
                            integrationInfo: IntegrationInfo)
    : RequestPresenter(view, clientApiTimeHelper, clientApiSessionEventsManager, clientApiCrashReportManager, gsonBuilder, integrationInfo), LibSimprintsContract.Presenter {

    override val mapDomainToLibSimprintErrorResponse: Map<ErrorResponse.Reason, Pair<Int, Intent?>>
        get() = mapOf(
            DIFFERENT_PROJECT_ID_SIGNED_IN to Pair(SIMPRINTS_INVALID_PROJECT_ID, null),
            DIFFERENT_PROJECT_ID_SIGNED_IN to Pair(SIMPRINTS_INVALID_PROJECT_ID, null),
            DIFFERENT_USER_ID_SIGNED_IN to Pair(SIMPRINTS_INVALID_USER_ID, null),
            GUID_NOT_FOUND_ONLINE to Pair(SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE, null),
            GUID_NOT_FOUND_OFFLINE to Pair(SIMPRINTS_VERIFY_GUID_NOT_FOUND_OFFLINE, null),
            UNEXPECTED_ERROR to Pair(SIMPRINTS_CANCELLED, null), //TODO: should we update LibSimpprints with the missing errors?,
            BLUETOOTH_NOT_SUPPORTED to Pair(SIMPRINTS_CANCELLED, null),
            SCANNER_LOW_BATTERY to Pair(SIMPRINTS_CANCELLED, null),
            UNKNOWN_BLUETOOTH_ISSUE to Pair(SIMPRINTS_CANCELLED, null),
            INVALID_CLIENT_REQUEST to Pair(SIMPRINTS_INVALID_INTENT_ACTION, null),
            INVALID_METADATA to Pair(SIMPRINTS_INVALID_METADATA, null),
            INVALID_MODULE_ID to Pair(SIMPRINTS_INVALID_MODULE_ID, null),
            INVALID_PROJECT_ID to Pair(SIMPRINTS_INVALID_PROJECT_ID, null),
            INVALID_SELECTED_ID to Pair(SIMPRINTS_CANCELLED, null),
            INVALID_SESSION_ID to Pair(SIMPRINTS_CANCELLED, null),
            INVALID_USER_ID to Pair(SIMPRINTS_INVALID_USER_ID, null),
            INVALID_VERIFY_ID to Pair(SIMPRINTS_INVALID_VERIFY_GUID, null))


    @SuppressLint("CheckResult")
    override fun start() {
        clientApiSessionEventsManager
            .createSession()
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


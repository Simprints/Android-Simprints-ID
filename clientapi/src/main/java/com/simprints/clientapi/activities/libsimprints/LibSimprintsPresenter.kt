package com.simprints.clientapi.activities.libsimprints

import android.annotation.SuppressLint
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.tools.json.GsonBuilder
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import io.reactivex.rxkotlin.subscribeBy


class LibSimprintsPresenter(private val view: LibSimprintsContract.View,
                            private val clientApiSessionEventsManager: ClientApiSessionEventsManager,
                            private val clientApiCrashReportManager: ClientApiCrashReportManager,
                            gsonBuilder: GsonBuilder,
                            private val action: String?,
                            integrationInfo: IntegrationInfo)
    : RequestPresenter(view, clientApiSessionEventsManager, clientApiCrashReportManager, gsonBuilder, integrationInfo), LibSimprintsContract.Presenter {

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
                    else -> view.returnIntentActionErrorToClient()
                }
            }.subscribeBy(
                onSuccess = { clientApiCrashReportManager.setSessionIdCrashlyticsKey(it) },
                onError = { it.printStackTrace() })
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

    override fun handleResponseError() = view.returnIntentActionErrorToClient()

}


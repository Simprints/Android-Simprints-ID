package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.responses.*


class CommCarePresenter(private val view: CommCareContract.View,
                        private val action: String?,
                        private val sessionEventsManager: ClientApiSessionEventsManager,
                        private val crashReportManager: ClientApiCrashReportManager)
    : RequestPresenter(view, sessionEventsManager), CommCareContract.Presenter {


    override val domainErrorToCallingAppResultCode: Map<ErrorResponse.Reason, Int>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.


    override fun handleEnrollResponse(enroll: EnrollResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

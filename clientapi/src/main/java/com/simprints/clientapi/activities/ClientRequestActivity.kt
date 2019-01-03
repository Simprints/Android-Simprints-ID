package com.simprints.clientapi.activities

import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.routers.ClientRequestErrorRouter
import com.simprints.clientapi.routers.SimprintsRequestRouter
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.libsimprints.Constants

abstract class ClientRequestActivity : AppCompatActivity(), ClientRequestView {

    override val enrollmentExtractor: EnrollmentExtractor
        get() = EnrollmentExtractor(intent)

    override val verifyExtractor: VerifyExtractor
        get() = VerifyExtractor(intent)

    override val identifyExtractor: IdentifyExtractor
        get() = IdentifyExtractor(intent)

    override fun sendSimprintsRequest(request: SimprintsIdRequest) =
        SimprintsRequestRouter.routeSimprintsRequest(this, request)

    override fun handleClientRequestError(exception: Exception) {
        ClientRequestErrorRouter.routeClientRequestError(this, exception)
        finish()
    }

    override fun returnIntentActionErrorToClient() {
        setResult(Constants.SIMPRINTS_INVALID_INTENT_ACTION, intent)
        finish()
    }

}

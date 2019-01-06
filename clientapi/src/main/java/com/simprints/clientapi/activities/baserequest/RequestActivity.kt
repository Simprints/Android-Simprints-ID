package com.simprints.clientapi.activities.baserequest

import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.routers.ClientRequestErrorRouter
import com.simprints.clientapi.routers.SimprintsRequestRouter.routeSimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.SimprintsConfirmationRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.libsimprints.Constants


abstract class RequestActivity : AppCompatActivity(), RequestContract.RequestView {

    override val enrollExtractor: EnrollExtractor
        get() = EnrollExtractor(intent)

    override val verifyExtractor: VerifyExtractor
        get() = VerifyExtractor(intent)

    override val identifyExtractor: IdentifyExtractor
        get() = IdentifyExtractor(intent)

    override val confirmIdentifyExtractor: ConfirmIdentifyExtractor
        get() = ConfirmIdentifyExtractor(intent)


    override fun sendSimprintsRequest(request: SimprintsIdRequest) {
        routeSimprintsIdRequest(this, request)

        if (request is SimprintsConfirmationRequest)
            finishAffinity()
    }

    override fun handleClientRequestError(exception: Exception) {
        ClientRequestErrorRouter.routeClientRequestError(this, exception)
        finish()
    }

    override fun returnIntentActionErrorToClient() {
        setResult(Constants.SIMPRINTS_INVALID_INTENT_ACTION, intent)
        finish()
    }

}

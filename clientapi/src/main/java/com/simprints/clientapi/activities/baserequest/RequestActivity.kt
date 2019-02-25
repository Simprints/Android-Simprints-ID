package com.simprints.clientapi.activities.baserequest

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.routers.ClientRequestErrorRouter
import com.simprints.clientapi.routers.SimprintsRequestRouter.routeSimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiConfirmationRequest
import com.simprints.clientapi.simprintsrequests.responses.*
import com.simprints.clientapi.simprintsrequests.responses.SimprintsIdResponse.Companion.BUNDLE_KEY
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

    override fun sendSimprintsRequest(request: ClientApiBaseRequest) {
        routeSimprintsIdRequest(this, request)

        if (request is ClientApiConfirmationRequest)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null)
            setResult(resultCode, data).also { finish() }
        else
            routeResponse(data.getParcelableExtra(BUNDLE_KEY))
    }

    protected fun sendOkResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun routeResponse(response: SimprintsIdResponse) = when (response) {
        is EnrollResponse -> presenter.handleEnrollResponse(response)
        is IdentificationResponse -> presenter.handleIdentifyResponse(response)
        is VerifyResponse -> presenter.handleVerifyResponse(response)
        is RefusalFormResponse -> presenter.handleRefusalResponse(response)
        else -> presenter.handleResponseError()
    }

}

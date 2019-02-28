package com.simprints.clientapi.activities.baserequest

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsConfirmation
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsRequest
import com.simprints.clientapi.routers.ClientRequestErrorRouter
import com.simprints.clientapi.simprintsrequests.requests.ClientApiAppRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiAppConfirmation
import com.simprints.clientapi.simprintsrequests.responses.ClientApiEnrollResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiIdentifyResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiRefusalFormResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiVerifyResponse
import com.simprints.libsimprints.Constants
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponse
import com.simprints.moduleinterfaces.clientapi.responses.IClientApiResponse.Companion.BUNDLE_KEY


abstract class RequestActivity : AppCompatActivity(), RequestContract.RequestView {

    override val enrollExtractor: EnrollExtractor
        get() = EnrollExtractor(intent)

    override val verifyExtractor: VerifyExtractor
        get() = VerifyExtractor(intent)

    override val identifyExtractor: IdentifyExtractor
        get() = IdentifyExtractor(intent)

    override val confirmIdentifyExtractor: ConfirmIdentifyExtractor
        get() = ConfirmIdentifyExtractor(intent)

    override fun sendSimprintsRequest(request: ClientApiAppRequest) =
        routeSimprintsRequest(this, request)

    override fun sendSimprintsConfirmationAndFinish(request: ClientApiAppConfirmation) {
        routeSimprintsConfirmation(this, request)
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

    private fun routeResponse(response: IClientApiResponse) = when (response) {
        is ClientApiEnrollResponse -> presenter.handleEnrollResponse(response)
        is ClientApiIdentifyResponse -> presenter.handleIdentifyResponse(response)
        is ClientApiVerifyResponse -> presenter.handleVerifyResponse(response)
        is ClientApiRefusalFormResponse -> presenter.handleRefusalResponse(response)
        else -> presenter.handleResponseError()
    }

}

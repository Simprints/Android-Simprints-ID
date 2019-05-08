package com.simprints.clientapi.activities.baserequest

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.exceptions.InvalidRequestException
import com.simprints.clientapi.extensions.toMap
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsConfirmation
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsRequest
import com.simprints.clientapi.routers.ClientRequestErrorRouter
import com.simprints.libsimprints.Constants
import com.simprints.moduleapi.app.responses.*
import com.simprints.moduleapi.app.responses.IAppResponse.Companion.BUNDLE_KEY


abstract class RequestActivity : AppCompatActivity(), RequestContract.RequestView {

    abstract val integrationInfo: IntegrationInfo

    override val enrollExtractor: EnrollExtractor
        get() = EnrollExtractor(intent)

    override val verifyExtractor: VerifyExtractor
        get() = VerifyExtractor(intent)

    override val identifyExtractor: IdentifyExtractor
        get() = IdentifyExtractor(intent)

    override val confirmIdentifyExtractor: ConfirmIdentifyExtractor
        get() = ConfirmIdentifyExtractor(intent)

    override fun sendSimprintsRequest(request: BaseRequest) =
        routeSimprintsRequest(this, request)

    override fun sendSimprintsConfirmationAndFinish(request: BaseConfirmation) {
        routeSimprintsConfirmation(this, request)
        finishAffinity()
    }

    override fun handleClientRequestError(exception: InvalidRequestException) {
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

    override fun getIntentAction() = intent.action ?: ""

    override fun getIntentExtras() = intent?.extras?.toMap()

    protected fun sendOkResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun routeResponse(response: IAppResponse) =
        when (response) {
            is IAppEnrolResponse -> presenter.handleEnrollResponse(EnrollResponse(response))
            is IAppIdentifyResponse -> presenter.handleIdentifyResponse(IdentifyResponse(response))
            is IAppVerifyResponse -> presenter.handleVerifyResponse(VerifyResponse(response))
            is IAppRefusalFormResponse -> presenter.handleRefusalResponse(RefusalFormResponse(response))
            else -> presenter.handleResponseError()
        }

}

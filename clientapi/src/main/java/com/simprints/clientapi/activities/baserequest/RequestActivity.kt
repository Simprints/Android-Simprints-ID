package com.simprints.clientapi.activities.baserequest

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.activities.errors.response.AlertActResponse
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.toMap
import com.simprints.clientapi.identity.GuidSelectionNotifier
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsConfirmation
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsRequest
import com.simprints.clientapi.routers.ClientRequestErrorRouter.launchAlert
import com.simprints.moduleapi.app.responses.*
import timber.log.Timber

abstract class RequestActivity : AppCompatActivity(), RequestContract.RequestView {

    abstract val guidSelectionNotifier: GuidSelectionNotifier

    override val action: String?
        get() = intent.action

    override val extras: Map<String, Any?>?
        get() = intent?.extras?.toMap()

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

    override fun sendSimprintsConfirmation(request: BaseConfirmation) {
        routeSimprintsConfirmation(this, request)
        guidSelectionNotifier.showMessage()
    }

    override fun handleClientRequestError(clientApiAlert: ClientApiAlert) {
        launchAlert(this, clientApiAlert)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse) = sendCancelResult()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("RequestActivity: onActivityResult")
        val isUnsuccessfulResponse = resultCode != Activity.RESULT_OK || data == null

        if (isUnsuccessfulResponse)
            sendCancelResult()
        else
            data?.let(::handleResponse)
    }

    protected fun sendOkResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun sendCancelResult() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun handleResponse(response: Intent) {
        response.getParcelableExtra<AlertActResponse>(AlertActResponse.BUNDLE_KEY)?.let {
            presenter.handleResponseError(ErrorResponse(it.clientApiAlert))
        } ?: routeAppResponse(response.getParcelableExtra(IAppResponse.BUNDLE_KEY))
    }

    private fun routeAppResponse(response: IAppResponse) = when (response.type) {
        IAppResponseType.ENROL -> presenter.handleEnrollResponse(EnrollResponse(response as IAppEnrolResponse))
        IAppResponseType.IDENTIFY -> presenter.handleIdentifyResponse(IdentifyResponse(response as IAppIdentifyResponse))
        IAppResponseType.VERIFY -> presenter.handleVerifyResponse(VerifyResponse(response as IAppVerifyResponse))
        IAppResponseType.REFUSAL -> presenter.handleRefusalResponse(RefusalFormResponse(response as IAppRefusalFormResponse))
        IAppResponseType.ERROR -> presenter.handleResponseError(ErrorResponse(response as IAppErrorResponse))
        IAppResponseType.CONFIRMATION -> presenter.handleConfirmationResponse(ConfirmationResponse(response as IAppConfirmationResponse))
    }

}

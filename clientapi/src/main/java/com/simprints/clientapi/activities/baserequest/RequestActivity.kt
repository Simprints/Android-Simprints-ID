package com.simprints.clientapi.activities.baserequest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simprints.clientapi.R
import com.simprints.clientapi.activities.BaseSplitActivity
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.activities.errors.response.AlertActResponse
import com.simprints.clientapi.clientrequests.extractors.*
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.ConfirmIdentityRequest
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.toMap
import com.simprints.clientapi.identity.GuidSelectionNotifier
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsRequest
import com.simprints.clientapi.routers.ClientRequestErrorRouter.launchAlert
import com.simprints.moduleapi.app.responses.*
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class RequestActivity : BaseSplitActivity(), RequestContract.RequestView {

    private var isActivityRestored = false
    private var requestProcessed = false

    abstract val guidSelectionNotifier: GuidSelectionNotifier

    override val intentAction: String?
        get() = intent.action

    override val extras: Map<String, Any?>?
        get() = intent?.extras?.toMap()

    override val enrolExtractor: EnrolExtractor
        get() = EnrolExtractor(intent)

    override val verifyExtractor: VerifyExtractor
        get() = VerifyExtractor(intent)

    override val identifyExtractor: IdentifyExtractor
        get() = IdentifyExtractor(intent)

    override val confirmIdentityExtractor: ConfirmIdentityExtractor
        get() = ConfirmIdentityExtractor(intent)

    override val enrolLastBiometricsExtractor: EnrolLastBiometricsExtractor
        get() = EnrolLastBiometricsExtractor(intent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_activity)
    }

    override fun sendSimprintsRequest(request: BaseRequest) {
        routeSimprintsRequest(this, request)
        if (request is ConfirmIdentityRequest) {
            guidSelectionNotifier.showMessage()
        }
    }

    override fun handleClientRequestError(clientApiAlert: ClientApiAlert) {
        launchAlert(this, clientApiAlert)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        isActivityRestored = true
    }

    override fun onResume() {
        super.onResume()
        if (!isActivityRestored && !requestProcessed) {
            requestProcessed = true
            lifecycleScope.launch { presenter.start() }
        }
    }

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
        IAppResponseType.ENROL, IAppResponseType.ENROL_LAST_BIOMETRICS -> presenter.handleEnrolResponse(EnrolResponse(response as IAppEnrolResponse))
        IAppResponseType.IDENTIFY -> presenter.handleIdentifyResponse(IdentifyResponse(response as IAppIdentifyResponse))
        IAppResponseType.VERIFY -> presenter.handleVerifyResponse(VerifyResponse(response as IAppVerifyResponse))
        IAppResponseType.REFUSAL -> presenter.handleRefusalResponse(RefusalFormResponse(response as IAppRefusalFormResponse))
        IAppResponseType.CONFIRMATION -> presenter.handleConfirmationResponse(ConfirmationResponse(response as IAppConfirmationResponse))
        IAppResponseType.ERROR -> presenter.handleResponseError(ErrorResponse(response as IAppErrorResponse))
    }
}

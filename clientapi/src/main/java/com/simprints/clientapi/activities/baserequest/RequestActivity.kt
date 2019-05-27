package com.simprints.clientapi.activities.baserequest

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.EnrollExtractor
import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromAlertTypeToDomain
import com.simprints.clientapi.extensions.toMap
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsConfirmation
import com.simprints.clientapi.routers.AppRequestRouter.routeSimprintsRequest
import com.simprints.clientapi.routers.ClientRequestErrorRouter.extractPotentialAlertScreenResponse
import com.simprints.clientapi.routers.ClientRequestErrorRouter.launchAlert
import com.simprints.moduleapi.app.responses.*
import com.simprints.moduleapi.app.responses.IAppResponse.Companion.BUNDLE_KEY
import timber.log.Timber


abstract class RequestActivity : AppCompatActivity(), RequestContract.RequestView {

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

    override fun handleClientRequestError(clientApiAlert: ClientApiAlert) {
        launchAlert(this, clientApiAlert)
    }

    override fun returnErrorToClient(resultCode: Int?) {
        resultCode?.let {
            setResult(it, intent)
        } ?: setResult(Activity.RESULT_CANCELED)

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("RequestActivity: onActivityResult")

        val potentialAlertScreenResponse = extractPotentialAlertScreenResponse(requestCode, resultCode, data)
        if (potentialAlertScreenResponse != null) {
            presenter.handleResponseError(ErrorResponse(fromAlertTypeToDomain(potentialAlertScreenResponse.clientApiAlert)))
        } else {
            if (resultCode != Activity.RESULT_OK || data == null)
                setResult(resultCode, data).also { finish() }
            else
                routeResponse(data.getParcelableExtra(BUNDLE_KEY))
        }
    }

    override fun getIntentAction() = intent.action ?: ""

    override fun getIntentExtras() = intent?.extras?.toMap()

    protected fun sendOkResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun routeResponse(response: IAppResponse) =
        when (response.type) {
            IAppResponseType.ENROL -> presenter.handleEnrollResponse(EnrollResponse(response as IAppEnrolResponse))
            IAppResponseType.IDENTIFY -> presenter.handleIdentifyResponse(IdentifyResponse(response as IAppIdentifyResponse))
            IAppResponseType.VERIFY -> presenter.handleVerifyResponse(VerifyResponse(response as IAppVerifyResponse))
            IAppResponseType.REFUSAL -> presenter.handleRefusalResponse(RefusalFormResponse(response as IAppRefusalFormResponse))
            IAppResponseType.ERROR -> presenter.handleResponseError(ErrorResponse(response as IAppErrorResponse))
        }

}

package com.simprints.clientapi.routers

import android.app.Activity
import android.content.Intent
import android.os.Build
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrollRequest
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.clientapi.domain.requests.VerifyRequest
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.requests.confirmations.IdentifyConfirmation
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.extensions.toIntent


object AppRequestRouter {

    private const val REGISTER_REQUEST_CODE = 97
    private const val IDENTIFY_REQUEST_CODE = 98
    private const val VERIFY_REQUEST_CODE = 99

    private const val REGISTER = "com.simprints.clientapp.REGISTER"
    private const val IDENTIFY = "com.simprints.clientapp.IDENTIFY"
    private const val VERIFY = "com.simprints.clientapp.VERIFY"
    private const val SELECT_GUID_INTENT = "com.simprints.clientapp.CONFIRM_IDENTITY"

    fun routeSimprintsRequest(act: Activity,
                              request: BaseRequest) = when (request) {
        // Regular Requests
        is EnrollRequest -> act.route(request, REGISTER, REGISTER_REQUEST_CODE)
        is VerifyRequest -> act.route(request, VERIFY, VERIFY_REQUEST_CODE)
        is IdentifyRequest -> act.route(request, IDENTIFY, IDENTIFY_REQUEST_CODE)

        // Handle Error
        else -> throw InvalidClientRequestException("Invalid Action AppRequest")
    }

    fun routeSimprintsConfirmation(act: Activity,
                                   request: BaseConfirmation) {
        when (request) {
            // Regular Requests
            is IdentifyConfirmation ->
                act.routeService(request.convertToAppRequest().toIntent(SELECT_GUID_INTENT))

            // Handle Error
            else -> throw InvalidClientRequestException("Invalid Confirmation AppRequest")
        }
    }

    private fun Activity.routeService(intent: Intent) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            this.startForegroundService(intent)
        else
            this.startService(intent)

    private fun Activity.route(request: BaseRequest, route: String, code: Int) =
        this.startActivityForResult(request.convertToAppRequest().toIntent(route), code)

}

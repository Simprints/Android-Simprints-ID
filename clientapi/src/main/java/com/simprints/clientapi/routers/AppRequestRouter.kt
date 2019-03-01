package com.simprints.clientapi.routers

import android.app.Activity
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.extensions.toIntent
import com.simprints.clientapi.simprintsrequests.requests.*


object AppRequestRouter {

    private const val REGISTER_REQUEST_CODE = 97
    private const val IDENTIFY_REQUEST_CODE = 98
    private const val VERIFY_REQUEST_CODE = 99

    private const val REGISTER = "com.simprints.clientapp.REGISTER"
    private const val IDENTIFY = "com.simprints.clientapp.IDENTIFY"
    private const val VERIFY = "com.simprints.clientapp.VERIFY"
    private const val SELECT_GUID_INTENT = "com.simprints.clientapp.CONFIRM_IDENTITY"

    fun routeSimprintsRequest(act: Activity,
                              request: ClientApiAppRequest) = when (request) {
        // Regular Requests
        is ClientApiEnrollRequest -> act.route(request, REGISTER, REGISTER_REQUEST_CODE)
        is ClientApiVerifyRequest -> act.route(request, VERIFY, VERIFY_REQUEST_CODE)
        is ClientApiIdentifyRequest -> act.route(request, IDENTIFY, IDENTIFY_REQUEST_CODE)

        // Handle Error
        else -> throw InvalidClientRequestException("Invalid Action Request")
    }

    fun routeSimprintsConfirmation(act: Activity,
                                   request: ClientApiAppConfirmation) {
        when (request) {
            // Regular Requests
            is ClientApiConfirmIdentifyRequest ->
                act.startService(request.convertToAppRequest().toIntent(SELECT_GUID_INTENT))

            // Handle Error
            else -> throw InvalidClientRequestException("Invalid Confirmation Request")
        }
    }

    private fun Activity.route(request: ClientApiAppRequest, route: String, code: Int) =
        this.startActivityForResult(request.convertToAppRequest().toIntent(route), code)

}

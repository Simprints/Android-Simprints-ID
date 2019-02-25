package com.simprints.clientapi.routers

import android.app.Activity
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.extensions.toIntent
import com.simprints.clientapi.simprintsrequests.requests.*


object SimprintsRequestRouter {

    const val REGISTER_REQUEST_CODE = 97
    const val IDENTIFY_REQUEST_CODE = 98
    const val VERIFY_REQUEST_CODE = 99

    private const val REGISTER = "com.simprints.clientapp.REGISTER"
    private const val IDENTIFY = "com.simprints.clientapp.IDENTIFY"
    private const val VERIFY = "com.simprints.clientapp.VERIFY"
    private const val SELECT_GUID_INTENT = "com.simprints.clientapp.CONFIRM_IDENTITY"

    fun routeSimprintsIdRequest(act: Activity, request: ClientApiBaseRequest) {
        when (request) {
            is ClientApiActionRequest -> routeSimprintsActionRequest(act, request)
            is ClientApiConfirmationRequest -> routeSimprintsConfirmationRequest(act, request)
            else -> throw InvalidClientRequestException("Invalid Request")
        }
    }

    private fun routeSimprintsActionRequest(act: Activity,
                                            request: ClientApiActionRequest) = when (request) {
        // Regular Requests
        is ClientApiEnrollRequest -> act.route(request, REGISTER, REGISTER_REQUEST_CODE)
        is ClientApiVerifyRequest -> act.route(request, VERIFY, VERIFY_REQUEST_CODE)
        is ClientApiIdentifyRequest -> act.route(request, IDENTIFY, IDENTIFY_REQUEST_CODE)

        // Handle Error
        else -> throw InvalidClientRequestException("Invalid Action Request")
    }

    private fun routeSimprintsConfirmationRequest(act: Activity,
                                                  request: ClientApiConfirmationRequest) = when (request) {
        // Regular Requests
        is ClientApiConfirmIdentifyRequest -> act.startService(request.toIntent(SELECT_GUID_INTENT))

        // Handle Error
        else -> throw InvalidClientRequestException("Invalid Confirmation Request")
    }

    private fun Activity.route(intent: ClientApiBaseRequest, route: String, code: Int) =
        this.startActivityForResult(intent.toIntent(route), code)

}

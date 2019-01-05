package com.simprints.clientapi.routers

import android.app.Activity
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.extensions.toIntent
import com.simprints.clientapi.simprintsrequests.*
import com.simprints.clientapi.simprintsrequests.legacy.LegacyConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyVerifyRequest


object SimprintsRequestRouter {

    const val REGISTER_REQUEST_CODE = 97
    const val IDENTIFY_REQUEST_CODE = 98
    const val VERIFY_REQUEST_CODE = 99
    const val ERROR_REQUEST_CODE = 100

    private const val REGISTER = "com.simprints.clientapp.REGISTER"
    private const val IDENTIFY = "com.simprints.clientapp.IDENTIFY"
    private const val VERIFY = "com.simprints.clientapp.VERIFY"
    private const val SELECT_GUID_INTENT = "com.simprints.clientapp.CONFIRM_IDENTITY"

    private const val LEGACY_REGISTER = "com.simprints.legacy.REGISTER"
    private const val LEGACY_IDENTIFY = "com.simprints.legacy.IDENTIFY"
    private const val LEGACY_VERIFY = "com.simprints.legacy.VERIFY"
    private const val LEGACY_SELECT_GUID_INTENT = "com.simprints.legacy.CONFIRM_IDENTITY"

    fun routeSimprintsIdRequest(act: Activity, request: SimprintsIdRequest) {
        when (request) {
            is SimprintsActionRequest -> routeSimprintsActionRequest(act, request)
            is SimprintsConfirmationRequest -> routeSimprintsConfirmationRequest(act, request)
            else -> throw InvalidClientRequestException("Invalid Request")
        }
    }

    private fun routeSimprintsActionRequest(act: Activity,
                                            request: SimprintsActionRequest) = when (request) {
        // Regular Requests
        is EnrollRequest -> act.route(request, REGISTER, REGISTER_REQUEST_CODE)
        is VerifyRequest -> act.route(request, VERIFY, VERIFY_REQUEST_CODE)
        is IdentifyRequest -> act.route(request, IDENTIFY, IDENTIFY_REQUEST_CODE)

        // Legacy Requests
        is LegacyEnrollRequest -> act.route(request, LEGACY_REGISTER, REGISTER_REQUEST_CODE)
        is LegacyVerifyRequest -> act.route(request, LEGACY_VERIFY, VERIFY_REQUEST_CODE)
        is LegacyIdentifyRequest -> act.route(request, LEGACY_IDENTIFY, IDENTIFY_REQUEST_CODE)

        // Handle Error
        else -> throw InvalidClientRequestException("Invalid Action Request")
    }

    private fun routeSimprintsConfirmationRequest(act: Activity,
                                                  request: SimprintsConfirmationRequest) = when (request) {
        // Regular Requests
        is ConfirmIdentifyRequest -> act.startService(request.toIntent(SELECT_GUID_INTENT))

        // Legacy Requests
        is LegacyConfirmIdentifyRequest -> act.startService(request.toIntent(LEGACY_SELECT_GUID_INTENT))

        // Handle Error
        else -> throw InvalidClientRequestException("Invalid Confirmation Request")
    }

    private fun Activity.route(intent: SimprintsIdRequest, route: String, code: Int) =
        this.startActivityForResult(intent.toIntent(route), code)

}

package com.simprints.clientapi.routers

import android.app.Activity
import com.simprints.clientapi.domain.requests.*
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.extensions.toIntent
import com.simprints.core.tools.extentions.removeAnimationsToNextActivity

object AppRequestRouter {

    private const val REGISTER_REQUEST_CODE = 97
    private const val IDENTIFY_REQUEST_CODE = 98
    private const val VERIFY_REQUEST_CODE = 99
    private const val CONFIRM_IDENTITY_REQUEST_CODE = 100

    private const val REGISTER = "com.simprints.clientapp.REGISTER"
    private const val IDENTIFY = "com.simprints.clientapp.IDENTIFY"
    private const val VERIFY = "com.simprints.clientapp.VERIFY"
    private const val CONFIRM_IDENTITY = "com.simprints.clientapp.CONFIRM_IDENTITY"

    fun routeSimprintsRequest(act: Activity,
                              request: BaseRequest) =
        when (request) {
            // Regular Requests
            is EnrollRequest -> act.route(request, REGISTER, REGISTER_REQUEST_CODE)
            is VerifyRequest -> act.route(request, VERIFY, VERIFY_REQUEST_CODE)
            is IdentifyRequest -> act.route(request, IDENTIFY, IDENTIFY_REQUEST_CODE)
            is ConfirmIdentityRequest -> act.route(request, CONFIRM_IDENTITY, CONFIRM_IDENTITY_REQUEST_CODE)
            else -> throw InvalidClientRequestException("Invalid Action AppRequest")
        }

    private fun Activity.route(request: BaseRequest, route: String, code: Int) {
        this.removeAnimationsToNextActivity()
        this.startActivityForResult(request.convertToAppRequest().toIntent(route), code)
    }
}

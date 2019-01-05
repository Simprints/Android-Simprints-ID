package com.simprints.clientapi.routers

import android.app.Activity
import android.content.Intent
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.simprintsrequests.*
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacySimprintsActionRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyVerifyRequest


object SimprintsRequestRouter {

    const val REGISTER_REQUEST_CODE = 97
    const val IDENTIFY_REQUEST_CODE = 98
    const val VERIFY_REQUEST_CODE = 99
    const val ERROR_REQUEST_CODE = 100

    private const val REGISTER = "com.simprints.clientapp.REGISTER"
    private const val IDENTIFY = "com.simprints.clientapp.IDENTIFY"
    private const val VERIFY = "com.simprints.clientapp.VERIFY"

    private const val LEGACY_REGISTER = "com.simprints.legacy.REGISTER"
    private const val LEGACY_IDENTIFY = "com.simprints.legacy.IDENTIFY"
    private const val LEGACY_VERIFY = "com.simprints.legacy.VERIFY"

    fun routeSimprintsActionRequest(act: Activity, request: SimprintsActionRequest) = when (request) {
        is EnrollRequest -> act.route(request, REGISTER, REGISTER_REQUEST_CODE)
        is VerifyRequest -> act.route(request, VERIFY, VERIFY_REQUEST_CODE)
        is IdentifyRequest -> act.route(request, IDENTIFY, IDENTIFY_REQUEST_CODE)
        // TODO handle error
        else -> act.startActivityForResult(Intent(act, ErrorActivity::class.java), ERROR_REQUEST_CODE)
    }

    fun routeLegacySimprintsRequest(act: Activity,
                                    request: LegacySimprintsActionRequest) = when (request) {
        is LegacyEnrollRequest -> act.route(request, LEGACY_REGISTER, REGISTER_REQUEST_CODE)
        is LegacyVerifyRequest -> act.route(request, LEGACY_VERIFY, VERIFY_REQUEST_CODE)
        is LegacyIdentifyRequest -> act.route(request, LEGACY_IDENTIFY, IDENTIFY_REQUEST_CODE)
        // TODO handle error
        else -> act.startActivityForResult(Intent(act, ErrorActivity::class.java), ERROR_REQUEST_CODE)
    }

    private fun SimprintsIdRequest.toIntent(action: String): Intent =
        Intent(action).apply { putExtra(this@toIntent.requestName, this@toIntent) }

    private fun Activity.route(intent: SimprintsIdRequest, route: String, code: Int) =
        this.startActivityForResult(intent.toIntent(route), code)

}

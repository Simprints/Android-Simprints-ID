package com.simprints.clientapi.routers

import android.app.Activity
import android.content.Intent
import com.simprints.clientapi.activities.errors.ErrorActivity
import com.simprints.clientapi.simprintsrequests.EnrollRequest
import com.simprints.clientapi.simprintsrequests.IdentifyRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.clientapi.simprintsrequests.VerifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyIdentifyRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacySimprintsIdRequest
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

    fun routeSimprintsRequest(act: Activity, request: SimprintsIdRequest) = when (request) {
        is EnrollRequest -> act.startActivityForResult(
            request.toIntent(REGISTER), REGISTER_REQUEST_CODE
        )
        is VerifyRequest -> act.startActivityForResult(
            request.toIntent(VERIFY), VERIFY_REQUEST_CODE
        )
        is IdentifyRequest -> act.startActivityForResult(
            request.toIntent(IDENTIFY), IDENTIFY_REQUEST_CODE
        )
        is LegacySimprintsIdRequest -> routeLegacySimprintsRequest(act, request)
        // TODO handle error
        else -> act.startActivityForResult(Intent(act, ErrorActivity::class.java), ERROR_REQUEST_CODE)
    }

    private fun routeLegacySimprintsRequest(act: Activity,
                                            request: LegacySimprintsIdRequest) = when (request) {
        is LegacyEnrollRequest -> act.startActivityForResult(
            request.toIntent(LEGACY_REGISTER), REGISTER_REQUEST_CODE
        )
        is LegacyVerifyRequest -> act.startActivityForResult(
            request.toIntent(LEGACY_VERIFY), VERIFY_REQUEST_CODE
        )
        is LegacyIdentifyRequest -> act.startActivityForResult(
            request.toIntent(LEGACY_IDENTIFY), IDENTIFY_REQUEST_CODE
        )
        // TODO handle error
        else -> act.startActivityForResult(Intent(act, ErrorActivity::class.java), ERROR_REQUEST_CODE)
    }

    private fun SimprintsIdRequest.toIntent(action: String): Intent =
        Intent(action).apply { putExtra(this@toIntent.requestName, this@toIntent) }

}

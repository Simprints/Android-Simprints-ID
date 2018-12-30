package com.simprints.clientapi.routers

import android.app.Activity
import android.content.Intent
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.simprintsrequests.EnrollmentRequest
import com.simprints.clientapi.simprintsrequests.legacy.LegacyEnrollmentRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


object SimprintsRequestRouter {

    const val REGISTER_REQUEST_CODE = 97
    const val IDENTIFY_REQUEST_CODE = 98
    const val VERIFY_REQUEST_CODE = 99
    const val ERROR_REQUEST_CODE = 100

    private const val REGISTER = "com.simprints.clientapp.REGISTER"
    private const val IDENTIFY = "com.simprints.clientapp.IDENTIFY"
    private const val UPDATE = "com.simprints.clientapp.UPDATE"
    private const val VERIFY = "com.simprints.clientapp.VERIFY"

    private const val LEGACY_REGISTER = "com.simprints.legacy.REGISTER"
    private const val LEGACY_IDENTIFY = "com.simprints.legacy.IDENTIFY"
    private const val LEGACY_UPDATE = "com.simprints.legacy.UPDATE"
    private const val LEGACY_VERIFY = "com.simprints.legacy.VERIFY"

    fun routeSimprintsRequest(activity: Activity, request: SimprintsIdRequest) = when (request) {
        is EnrollmentRequest -> activity.startActivityForResult(
            request.toIntent(REGISTER), REGISTER_REQUEST_CODE
        )
        is LegacyEnrollmentRequest -> activity.startActivityForResult(
            request.toIntent(LEGACY_REGISTER), REGISTER_REQUEST_CODE
        )
        else -> activity.startActivityForResult(Intent(activity, OdkActivity::class.java), ERROR_REQUEST_CODE)
    }

    private fun SimprintsIdRequest.toIntent(action: String): Intent =
        Intent(action).apply { putExtra(this@toIntent.requestName, this@toIntent) }

}

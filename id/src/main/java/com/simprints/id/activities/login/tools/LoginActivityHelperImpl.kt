package com.simprints.id.activities.login.tools

import android.content.Intent
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.activities.login.response.QrCodeResponse
import com.simprints.id.tools.InternalConstants.QrCapture.Companion.QR_SCAN_ERROR_KEY
import com.simprints.id.tools.InternalConstants.QrCapture.Companion.QR_SCAN_RESULT_KEY
import com.simprints.id.tools.InternalConstants.QrCapture.QrCaptureError
import javax.inject.Inject

class LoginActivityHelperImpl @Inject constructor(private val jsonHelper: JsonHelper) :
    LoginActivityHelper {

    override fun areMandatoryCredentialsPresent(
        projectId: String,
        projectSecret: String,
        userId: String
    ): Boolean {
        return projectId.isNotEmpty()
            && projectSecret.isNotEmpty()
            && userId.isNotEmpty()
    }

    override fun areSuppliedProjectIdAndProjectIdFromIntentEqual(
        suppliedProjectId: String,
        projectIdFromIntent: String
    ): Boolean {
        return suppliedProjectId == projectIdFromIntent
    }

    override fun tryParseQrCodeResponse(response: Intent): QrCodeResponse {
        val qrValue = response.getStringExtra(QR_SCAN_RESULT_KEY)
        return qrValue?.let { jsonHelper.fromJson<QrCodeResponse>(it) } ?: throw Throwable("qrValue null")
    }

    override fun tryParseQrCodeError(response: Intent): QrCaptureError {
        val error = response.getSerializableExtra(QR_SCAN_ERROR_KEY)
        return if (error is QrCaptureError) {
            error
        } else {
            QrCaptureError.GENERAL_ERROR
        }
    }
}

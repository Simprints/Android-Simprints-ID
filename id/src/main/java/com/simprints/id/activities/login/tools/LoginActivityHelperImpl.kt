package com.simprints.id.activities.login.tools

import android.content.Intent
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.activities.login.response.QrCodeResponse

class LoginActivityHelperImpl(
    private val jsonHelper: JsonHelper
) : LoginActivityHelper {

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
        val qrValue = response.getStringExtra(EXTRA_SCAN_RESULT)
        return qrValue?.let { jsonHelper.fromJson<QrCodeResponse>(it) } ?: throw Throwable("qrValue null")
    }

    private companion object {
        const val EXTRA_SCAN_RESULT = "SCAN_RESULT"
    }

}

package com.simprints.id.activities.login.tools

import android.content.Intent
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.activities.login.response.CredentialsResponse

class LoginActivityHelperImpl : LoginActivityHelper {

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

    override fun tryParseQrCodeResponse(response: Intent): CredentialsResponse {
        val qrValue = response.getStringExtra(EXTRA_SCAN_RESULT)
        return JsonHelper.fromJson(qrValue)
    }

    private companion object {
        const val EXTRA_SCAN_RESULT = "SCAN_RESULT"
    }

}

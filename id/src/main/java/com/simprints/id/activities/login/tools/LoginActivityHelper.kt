package com.simprints.id.activities.login.tools

import android.content.Intent
import com.simprints.id.activities.login.response.QrCodeResponse
import org.json.JSONException

interface LoginActivityHelper {

    fun areMandatoryCredentialsPresent(
        projectId: String,
        projectSecret: String,
        userId: String
    ): Boolean

    fun areSuppliedProjectIdAndProjectIdFromIntentEqual(
        suppliedProjectId: String,
        projectIdFromIntent: String
    ): Boolean

    /**
     * Valid scanned QR code format:
     * {"projectId":"someProjectId","projectSecret":"someSecret","backend":"https://some-url"}
     **/
    @Throws(JSONException::class)
    fun tryParseQrCodeResponse(response: Intent): QrCodeResponse

    fun isSecurityStatusRunning(): Boolean

}

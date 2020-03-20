package com.simprints.id.activities.login.tools

import android.content.Intent
import com.simprints.id.activities.login.response.CredentialsResponse
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
     * Valid Scanned Text Format:
     * {"projectId":"someProjectId","projectSecret":"someSecret"}
     **/
    @Throws(JSONException::class)
    fun tryParseQrCodeResponse(response: Intent): CredentialsResponse

}

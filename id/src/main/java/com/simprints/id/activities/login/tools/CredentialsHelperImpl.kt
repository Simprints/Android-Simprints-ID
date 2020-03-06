package com.simprints.id.activities.login.tools

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.activities.login.CredentialsResponse

class CredentialsHelperImpl : CredentialsHelper {

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

    override fun tryParseQrCodeResponse(qrValue: String): CredentialsResponse {
        return JsonHelper.fromJson(qrValue)
    }

}

package com.simprints.id.activities.login.tools

import com.simprints.id.activities.login.CredentialsResponse

interface CredentialsHelper {

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
    fun tryParseQrCodeResponse(qrValue: String): CredentialsResponse

}

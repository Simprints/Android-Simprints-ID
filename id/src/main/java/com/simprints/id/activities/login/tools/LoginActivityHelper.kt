package com.simprints.id.activities.login.tools

import android.content.Intent
import android.content.pm.PackageManager
import com.simprints.id.activities.login.CredentialsResponse

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

    fun tryGetScannerAppIntent(packageManager: PackageManager): Intent?

    fun getIntentForScannerAppOnPlayStore(): Intent

    /**
     * Valid Scanned Text Format:
     * {"projectId":"someProjectId","projectSecret":"someSecret"}
     **/
    fun tryParseQrCodeResponse(response: Intent): CredentialsResponse

}

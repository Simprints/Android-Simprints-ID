package com.simprints.id.activities.login.tools

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.activities.login.CredentialsResponse

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

    override fun tryGetScannerAppIntent(packageManager: PackageManager): Intent? {
        val intent = Intent(QR_SCAN_ACTION)
            .putExtra("SAVE_HISTORY", false)
            .putExtra("SCAN_MODE", "QR_CODE_MODE")

        val isScannerAppInstalled = intent.resolveActivity(packageManager) != null

        return if (isScannerAppInstalled) intent else null
    }

    override fun getIntentForScannerAppOnPlayStore(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_LINK_TO_SCANNER_APP))
    }

    override fun tryParseQrCodeResponse(response: Intent): CredentialsResponse {
        val qrValue = response.getStringExtra(KEY_SCAN_RESULT)
        return JsonHelper.fromJson(qrValue)
    }

    private companion object {
        const val KEY_SCAN_RESULT = "SCAN_RESULT"
        const val QR_SCAN_ACTION = "com.google.zxing.client.android.SCAN"
        const val GOOGLE_PLAY_LINK_TO_SCANNER_APP =
            "https://play.google.com/store/apps/details?id=com.google.zxing.client.android"
    }

}

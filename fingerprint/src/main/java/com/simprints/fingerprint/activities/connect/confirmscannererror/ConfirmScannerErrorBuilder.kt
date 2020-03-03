package com.simprints.fingerprint.activities.connect.confirmscannererror

import android.app.AlertDialog
import android.content.Context
import com.simprints.fingerprint.R
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper

class ConfirmScannerErrorBuilder {

    fun build(context: Context,
              fingerprintAndroidResourcesHelper: FingerprintAndroidResourcesHelper,
              scannerId: String,
              onYes: () -> Unit,
              onNo: () -> Unit): AlertDialog =

        AlertDialog.Builder(context)
            .setTitle(getErrorTitleText(fingerprintAndroidResourcesHelper, scannerId))
            .setPositiveButton(getConfirmationYesMessage(fingerprintAndroidResourcesHelper)) { _, _ -> onYes() }
            .setNegativeButton(getConfirmationNoMessage(fingerprintAndroidResourcesHelper)) { _, _ -> onNo() }
            .setCancelable(false).create()

    private fun getConfirmationNoMessage(fingerprintAndroidResourcesHelper: FingerprintAndroidResourcesHelper) =
        fingerprintAndroidResourcesHelper.getString(R.string.scanner_confirmation_no)

    private fun getConfirmationYesMessage(fingerprintAndroidResourcesHelper: FingerprintAndroidResourcesHelper) =
        fingerprintAndroidResourcesHelper.getString(R.string.scanner_confirmation_yes)

    private fun getErrorTitleText(fingerprintAndroidResourcesHelper: FingerprintAndroidResourcesHelper, scannerId: String) =
        fingerprintAndroidResourcesHelper.getString(R.string.scanner_id_confirmation_message).format(scannerId)
}

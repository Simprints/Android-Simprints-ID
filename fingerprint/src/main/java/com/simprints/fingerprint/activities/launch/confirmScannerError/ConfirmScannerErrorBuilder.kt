package com.simprints.fingerprint.activities.launch.confirmScannerError

import android.app.AlertDialog
import android.content.Context
import com.simprints.fingerprint.R
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper

class ConfirmScannerErrorBuilder(val androidResourcesHelper: FingerprintAndroidResourcesHelper) {

    fun build(context: Context,
              scannerId: String,
              onYes: () -> Unit,
              onNo: () -> Unit): AlertDialog =

        AlertDialog.Builder(context)
            .setTitle(getErrorTitleText(scannerId))
            .setPositiveButton(getConfirmationYesMessage()) { _, _ -> onYes() }
            .setNegativeButton(getConfirmationNoMessage()) { _, _ -> onNo() }
            .setCancelable(false).create()

    private fun getConfirmationNoMessage() =
        androidResourcesHelper.getString(R.string.scanner_confirmation_no)

    private fun getConfirmationYesMessage() =
        androidResourcesHelper.getString(R.string.scanner_confirmation_yes)

    private fun getErrorTitleText(scannerId: String) =
        androidResourcesHelper.getString(R.string.scanner_id_comfirmation_message).format(scannerId)
}

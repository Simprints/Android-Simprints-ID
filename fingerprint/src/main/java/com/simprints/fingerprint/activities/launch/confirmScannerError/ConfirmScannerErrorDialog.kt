package com.simprints.fingerprint.activities.launch.confirmScannerError

import android.app.AlertDialog
import android.content.Context
import com.simprints.fingerprint.R

class ConfirmScannerErrorDialog(private val context: Context,
                                private val scannerId: String,
                                private val callbackYes: () -> Unit,
                                private val callbackNo: () -> Unit) {
    fun create(): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(getErrorTitleText())
            .setPositiveButton(getConfirmationYesMessage()) { _, _ -> callbackYes() }
            .setNegativeButton(getConfirmationNoMessage()) { _, _ -> callbackNo() }
            .setCancelable(false).create()

    private fun getConfirmationNoMessage() =
        context.resources.getString(R.string.scanner_confirmation_no)

    private fun getConfirmationYesMessage() =
        context.resources.getString(R.string.scanner_confirmation_yes)

    private fun getErrorTitleText() =
        context.getString(R.string.scanner_id_comfirmation_message).format(scannerId)
}

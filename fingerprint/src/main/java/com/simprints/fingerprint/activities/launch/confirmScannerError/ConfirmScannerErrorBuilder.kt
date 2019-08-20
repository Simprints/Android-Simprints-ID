package com.simprints.fingerprint.activities.launch.confirmScannerError

import android.app.AlertDialog
import android.content.Context
import com.simprints.fingerprint.R

class ConfirmScannerErrorBuilder {

    fun build(context: Context,
              scannerId: String,
              onYes: () -> Unit,
              onNo: () -> Unit): AlertDialog =

        AlertDialog.Builder(context)
            .setTitle(getErrorTitleText(context, scannerId))
            .setPositiveButton(getConfirmationYesMessage(context)) { _, _ -> onYes() }
            .setNegativeButton(getConfirmationNoMessage(context)) { _, _ -> onNo() }
            .setCancelable(false).create()

    private fun getConfirmationNoMessage(context: Context) =
        context.getString(R.string.scanner_confirmation_no)

    private fun getConfirmationYesMessage(context: Context) =
        context.getString(R.string.scanner_confirmation_yes)

    private fun getErrorTitleText(context: Context, scannerId: String) =
        context.getString(R.string.scanner_id_comfirmation_message).format(scannerId)
}

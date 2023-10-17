package com.simprints.fingerprint.activities.connect.confirmscannererror

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R

@ExcludedFromGeneratedTestCoverageReports("UI code")
class ConfirmScannerErrorBuilder {

    fun build(context: Context,
              scannerId: String,
              onYes: () -> Unit,
              onNo: () -> Unit): AlertDialog =

        MaterialAlertDialogBuilder(context)
            .setTitle(getErrorTitleText(context, scannerId))
            .setPositiveButton(getConfirmationYesMessage(context)) { _, _ -> onYes() }
            .setNegativeButton(getConfirmationNoMessage(context)) { _, _ -> onNo() }
            .setCancelable(false)
            .create()

    private fun getConfirmationNoMessage(context: Context) =
        context.getString(R.string.scanner_confirmation_no)

    private fun getConfirmationYesMessage(context: Context) =
        context.getString(R.string.scanner_confirmation_yes)

    private fun getErrorTitleText(context: Context, scannerId: String) =
        context.getString(R.string.scanner_id_confirmation_message).format(scannerId)
}

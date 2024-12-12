package com.simprints.fingerprint.capture.views.confirmfingerprints

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.resources.nameTextId
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class ConfirmFingerprintsDialog(
    private val context: Context,
    private val scannedFingers: List<Item>,
    private val onConfirm: () -> Unit,
    private val onRestart: () -> Unit,
) {
    fun create(): AlertDialog = MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(IDR.string.fingerprint_capture_confirm_fingers_dialog_title))
        .setMessage(getMapOfFingersAndQualityAsText())
        .setPositiveButton(context.getString(IDR.string.fingerprint_capture_confirm_fingers_dialog_yes)) { _, _ -> onConfirm() }
        .setNegativeButton(context.getString(IDR.string.fingerprint_capture_confirm_fingers_dialog_no)) { _, _ -> onRestart() }
        .setCancelable(false)
        .create()

    @SuppressLint("DefaultLocale")
    private fun getMapOfFingersAndQualityAsText(): String = StringBuilder()
        .also {
            scannedFingers.forEach { (fingerName, successes, scans) ->
                if (scans == 1) {
                    it.append(if (successes == 1) "✓ " else "× ")
                } else {
                    it.append("$successes / $scans ")
                }
                it.append(context.getString(fingerName.nameTextId()) + "\n")
            }
        }.toString()

    data class Item(
        val finger: IFingerIdentifier,
        val numberOfSuccessfulScans: Int,
        val numberOfScans: Int,
    )
}

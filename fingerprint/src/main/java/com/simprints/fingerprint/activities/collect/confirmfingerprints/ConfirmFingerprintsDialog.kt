package com.simprints.fingerprint.activities.collect.confirmfingerprints

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import com.simprints.fingerprint.R

class ConfirmFingerprintsDialog(private val context: Context,
                                private val scannedFingers: Map<String, Boolean>,
                                private val callbackConfirm: () -> Unit,
                                private val callbackRestart: () -> Unit) {

    fun create(): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.confirm_fingers_dialog_title))
            .setMessage(getMapOfFingersAndQualityAsText())
            .setPositiveButton(context.getString(R.string.confirm)) { _, _ -> callbackConfirm() }
            .setNegativeButton(context.getString(R.string.restart)) { _, _ -> callbackRestart() }
            .setCancelable(false).create()

    @SuppressLint("DefaultLocale")
    private fun getMapOfFingersAndQualityAsText(): String =
        StringBuilder().also {
            scannedFingers.forEach { (fingerName, scanThresholdPassed) ->
                it.append(if (scanThresholdPassed) "✓ " else "× ")
                it.append(fingerName.toUpperCase() + "\n")
            }
        }.toString()
}

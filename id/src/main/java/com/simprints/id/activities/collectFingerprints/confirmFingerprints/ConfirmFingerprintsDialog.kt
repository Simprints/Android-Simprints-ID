package com.simprints.id.activities.collectFingerprints.confirmFingerprints

import android.app.AlertDialog
import android.content.Context
import com.simprints.id.R

class ConfirmFingerprintsDialog(private val context: Context,
                                private val scannedFingers: MutableMap<String, Boolean>,
                                private val callbackConfirm: () -> Unit,
                                private val callbackRestart: () -> Unit) {

    fun create(): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.confirm_fingers_dialog_title))
            .setMessage(getMapOfFingersAndQualityAsText())
            .setPositiveButton(context.resources.getString(R.string.confirm)) { _, _ -> callbackConfirm() }
            .setNegativeButton(context.resources.getString(R.string.restart)) { _, _ -> callbackRestart() }
            .setCancelable(false).create()

    private fun getMapOfFingersAndQualityAsText(): String =
        StringBuilder().also {
            scannedFingers.forEach { fingerName, scanThresholdPassed ->
                it.append(if (scanThresholdPassed) "✓ " else "× ")
                it.append(fingerName + "\n")
            }
        }.toString()
}

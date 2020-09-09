package com.simprints.fingerprint.activities.collect.confirmfingerprints

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.resources.nameTextId
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

class ConfirmFingerprintsDialog(private val context: Context,
                                private val scannedFingers: List<Item>,
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
            scannedFingers.forEach { (fingerName, successes, scans) ->
                if (scans == 1) {
                    it.append(if (successes == 1) "✓ " else "× ")
                } else {
                    it.append("$successes / $scans ")
                }
                it.append(context.getString(fingerName.nameTextId()) + "\n")
            }
        }.toString()

    data class Item(val finger: FingerIdentifier, val numberOfSuccessfulScans: Int, val numberOfScans: Int)
}

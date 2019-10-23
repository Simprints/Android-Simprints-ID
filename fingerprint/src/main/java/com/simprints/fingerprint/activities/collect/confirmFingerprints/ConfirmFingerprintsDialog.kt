package com.simprints.fingerprint.activities.collect.confirmFingerprints

import android.app.AlertDialog
import android.content.Context
import com.simprints.fingerprint.R
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper

class ConfirmFingerprintsDialog(private val context: Context,
                                private val androidResourcesHelper: FingerprintAndroidResourcesHelper,
                                private val scannedFingers: MutableMap<String, Boolean>,
                                private val callbackConfirm: () -> Unit,
                                private val callbackRestart: () -> Unit) {

    fun create(): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(androidResourcesHelper.getString(R.string.confirm_fingers_dialog_title))
            .setMessage(getMapOfFingersAndQualityAsText())
            .setPositiveButton(androidResourcesHelper.getString(R.string.confirm)) { _, _ -> callbackConfirm() }
            .setNegativeButton(androidResourcesHelper.getString(R.string.restart)) { _, _ -> callbackRestart() }
            .setCancelable(false).create()

    private fun getMapOfFingersAndQualityAsText(): String =
        StringBuilder().also {
            scannedFingers.forEach { (fingerName, scanThresholdPassed) ->
                it.append(if (scanThresholdPassed) "✓ " else "× ")
                it.append(fingerName.toUpperCase() + "\n")
            }
        }.toString()
}

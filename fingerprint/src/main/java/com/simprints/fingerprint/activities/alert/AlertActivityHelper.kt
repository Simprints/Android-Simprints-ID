package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.content.Intent
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.fingerprint.activities.alert.AlertError.Companion.PAYLOAD_KEY
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import java.util.concurrent.atomic.AtomicBoolean

internal class AlertActivityHelper {

    private val settingsOpenedForPairing = AtomicBoolean(false)

    fun handleResume(retry: () -> Unit) {
        if (settingsOpenedForPairing.getAndSet(false)) {
            retry()
        }
    }

    fun handleAlertResult(
        activity: Activity,
        result: AlertResult,
        showRefusal: () -> Unit,
        retry: () -> Unit,
    ) {
        val alertError = result.payload
            .getString(PAYLOAD_KEY)
            ?.let { AlertError.valueOf(it) }
            ?: AlertError.UNEXPECTED_ERROR

        when (result.buttonKey) {
            AlertContract.ALERT_BUTTON_PRESSED_BACK -> {
                if (alertError == AlertError.UNEXPECTED_ERROR) {
                    finishWithError(activity, alertError)
                } else {
                    showRefusal()
                }
            }

            AlertError.ACTION_PAIR -> {
                settingsOpenedForPairing.set(true)
                openBluetoothSettings(activity)
            }

            AlertError.ACTION_CLOSE -> finishWithError(activity, alertError)
            AlertError.ACTION_REFUSAL -> showRefusal()
            AlertError.ACTION_RETRY -> retry()
            AlertError.ACTION_BT_SETTINGS -> openBluetoothSettings(activity)
        }
    }

    private fun finishWithError(
        activity: Activity,
        alertError: AlertError = AlertError.UNEXPECTED_ERROR,
    ) {
        activity.setResult(ResultCode.ALERT.value, Intent().apply {
            putExtra(AlertTaskResult.BUNDLE_KEY, AlertTaskResult(alertError))
        })
        activity.finish()
    }

    private fun openBluetoothSettings(activity: Activity) {
        activity.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

}

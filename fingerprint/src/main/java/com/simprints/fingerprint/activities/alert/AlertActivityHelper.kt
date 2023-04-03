package com.simprints.fingerprint.activities.alert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.simprints.feature.alert.AlertContract
import com.simprints.fingerprint.activities.alert.AlertError.Companion.PAYLOAD_KEY
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.orchestrator.domain.RequestCode
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
        data: Bundle,
        retry: () -> Unit,
    ) {
        val alertError = AlertContract.getResponsePayload(data)
            .getString(PAYLOAD_KEY)
            ?.let { AlertError.valueOf(it) }
            ?: AlertError.UNEXPECTED_ERROR

        when (AlertContract.getResponseKey(data)) {
            AlertContract.ALERT_BUTTON_PRESSED_BACK -> {
                if (alertError == AlertError.UNEXPECTED_ERROR) {
                    finishWithError(activity, alertError)
                } else {
                    goToRefusalActivity(activity)
                }
            }
            AlertError.ACTION_PAIR -> {
                settingsOpenedForPairing.set(true)
                openBluetoothSettings(activity)
            }
            AlertError.ACTION_CLOSE -> finishWithError(activity, alertError)
            AlertError.ACTION_REFUSAL -> goToRefusalActivity(activity)
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

    fun goToRefusalActivity(activity: Activity) {
        activity.startActivityForResult(Intent(activity, RefusalActivity::class.java), RequestCode.REFUSAL.value)
    }

    private fun openBluetoothSettings(activity: Activity) {
        activity.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

}

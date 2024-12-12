package com.simprints.fingerprint.connect.screens.alert

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import java.util.concurrent.atomic.AtomicBoolean

internal class AlertActivityHelper {
    private val settingsOpenedForPairing = AtomicBoolean(false)
    private val appSettingsOpened = AtomicBoolean(false)

    fun handleResume(retry: () -> Unit) {
        listOf(settingsOpenedForPairing, appSettingsOpened)
            .any { it.getAndSet(false) }
            .takeIf { it }
            ?.run { retry() }
    }

    fun handleAlertResult(
        activity: Activity,
        result: AlertResult,
        showRefusal: () -> Unit,
        retry: () -> Unit,
        finishWithError: () -> Unit,
    ) {
        when (result.buttonKey) {
            AlertContract.ALERT_BUTTON_PRESSED_BACK -> {
                if (AppErrorReason.UNEXPECTED_ERROR == result.appErrorReason) {
                    finishWithError()
                } else {
                    showRefusal()
                }
            }

            AlertError.ACTION_PAIR -> openBluetoothSettings(activity)
            AlertError.ACTION_CLOSE -> finishWithError()
            AlertError.ACTION_REFUSAL -> showRefusal()
            AlertError.ACTION_RETRY -> retry()
            AlertError.ACTION_BT_SETTINGS -> openBluetoothSettings(activity)
            AlertError.ACTION_APP_SETTINGS -> openAppSettings(activity)
        }
    }

    private fun openBluetoothSettings(activity: Activity) {
        settingsOpenedForPairing.set(true)
        activity.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    private fun openAppSettings(activity: Activity) = with(activity) {
        appSettingsOpened.set(true)
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName"),
            ),
        )
    }
}

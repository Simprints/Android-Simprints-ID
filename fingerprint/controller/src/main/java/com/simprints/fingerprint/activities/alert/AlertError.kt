package com.simprints.fingerprint.activities.alert

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.simprints.feature.alert.alertButton
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertError.*
import com.simprints.fingerprint.controllers.core.eventData.model.fromFingerprintAlertToAlertTypeEvent
import com.simprints.infra.resources.R as IDR

/**
 * This class represent the display model for the error alert that occurred while a fingerprint
 * request was being processed.
 *
 * - [BLUETOOTH_NOT_SUPPORTED]  an alert indicating that the device doesn't support bluetooth
 * - [BLUETOOTH_NOT_ENABLED]  an alert indicating that the bluetooth isn't enabled
 * - [NOT_PAIRED]  an alert indicating the device isn't paired with the scanner
 * - [MULTIPLE_PAIRED_SCANNERS]  an alert indicating multiple scanners are paired to the device
 * - [DISCONNECTED]  an alert indicating a scanner is disconnected, usually duringa capture process
 * - [LOW_BATTERY]  an alert indicating that the scanner has a low batter
 * - [UNEXPECTED_ERROR]  an unexpected error occurred while processing fingerprint request
 *
 * @property title  the string resource value representing the tilte of the error
 * @property message  the string resource value to be displayed on the scree
 * @property backgroundColor  the color resource value representing screen's background
 * @property mainDrawable  the image icon to be shown based on the error type
 * @property hintDrawable  the image icon hinting what kind of error occured
 * @property leftButton  the specific action to be triggered when the left button is clicked
 * @property rightButton  the specific action to be triggered when the right button is clicked
 */
enum class AlertError(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val backgroundColor: AlertColor = AlertColor.Default,
    @DrawableRes val mainDrawable: Int,
    @DrawableRes val hintDrawable: Int? = null,
    val leftButton: AlertButtonConfig,
    val rightButton: AlertButtonConfig? = null,
) {

    // Bluetooth errors
    BLUETOOTH_NOT_SUPPORTED(
        title = R.string.error_occurred_title,
        message = R.string.bluetooth_not_supported_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        leftButton = Buttons.closeButton(),
    ),

    BLUETOOTH_NOT_ENABLED(
        title = R.string.scanner_error_turn_scanner_on_title,
        message = R.string.bluetooth_not_enabled_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        leftButton = Buttons.tryAgainButton(),
        rightButton = Buttons.bluetoothSettingsButton()
    ),

    BLUETOOTH_NO_PERMISSION(
        title = R.string.no_permission_title,
        message = R.string.bluetooth_no_permission_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        leftButton = Buttons.appSettingsButton()
    ),

    NOT_PAIRED(
        title = R.string.bluetooth_error_pair_scanner_and_device_title,
        message = R.string.bluetooth_error_pair_scanner_and_device_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        leftButton = Buttons.pairScannerButton(),
    ),

    MULTIPLE_PAIRED_SCANNERS(
        title = R.string.scanner_error_turn_scanner_on_title,
        message = R.string.multiple_scanners_found_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = R.drawable.multiple_scanners_found,
        leftButton = Buttons.tryAgainButton(),
        rightButton = Buttons.bluetoothSettingsButton()
    ),

    // Scanner errors
    DISCONNECTED(
        title = R.string.scanner_error_turn_scanner_on_title,
        message = R.string.scanner_error_turn_scanner_on_message,
        backgroundColor = AlertColor.Default,
        mainDrawable = R.drawable.scanner_error_icon,
        leftButton = Buttons.tryAgainButton(),
    ),

    LOW_BATTERY(
        title = R.string.low_battery_title,
        message = R.string.low_battery_message,
        backgroundColor = AlertColor.Default,
        mainDrawable = R.drawable.scanner_error_icon,
        hintDrawable = IDR.drawable.ic_alert_hint_battery,
        leftButton = Buttons.closeWithRefusalButton()
    ),

    //Unexpected errors
    UNEXPECTED_ERROR(
        title = R.string.error_occurred_title,
        message = R.string.unforeseen_error_message,
        backgroundColor = AlertColor.Red,
        mainDrawable = IDR.drawable.ic_alert_default,
        leftButton = Buttons.closeButton(),
    );

    private object Buttons {

        fun closeButton() = alertButton {
            textRes = R.string.close
            closeOnClick = true
            resultKey = ACTION_CLOSE
        }

        fun closeWithRefusalButton() = alertButton {
            textRes = R.string.close
            closeOnClick = true
            resultKey = ACTION_REFUSAL
        }

        fun bluetoothSettingsButton() = alertButton {
            textRes = R.string.settings_label
            closeOnClick = true
            resultKey = ACTION_BT_SETTINGS
        }

        fun appSettingsButton() = alertButton {
            textRes = R.string.settings_label
            closeOnClick = true
            resultKey = ACTION_APP_SETTINGS
        }

        fun tryAgainButton() = alertButton {
            textRes = R.string.try_again_label
            closeOnClick = true
            resultKey = ACTION_RETRY
        }

        fun pairScannerButton() = alertButton {
            textRes = R.string.pair_scanner_label
            closeOnClick = true
            resultKey = ACTION_PAIR
        }
    }

    fun toAlertConfig() = alertConfiguration {
        color = this@AlertError.backgroundColor
        titleRes = this@AlertError.title
        image = this@AlertError.mainDrawable
        messageRes = this@AlertError.message
        messageIcon = this@AlertError.hintDrawable
        eventType = this@AlertError.fromFingerprintAlertToAlertTypeEvent()

        leftButton = this@AlertError.leftButton
        rightButton = this@AlertError.rightButton

        payload = bundleOf(PAYLOAD_KEY to this@AlertError.name)
    }

    companion object {

        internal const val PAYLOAD_KEY = "alert_payload"

        internal const val ACTION_CLOSE = "action_close"
        internal const val ACTION_RETRY = "action_retry"
        internal const val ACTION_REFUSAL = "action_refusal"
        internal const val ACTION_BT_SETTINGS = "action_bt_settings"
        internal const val ACTION_APP_SETTINGS = "action_app_settings"
        internal const val ACTION_PAIR = "action_pair"
    }

}

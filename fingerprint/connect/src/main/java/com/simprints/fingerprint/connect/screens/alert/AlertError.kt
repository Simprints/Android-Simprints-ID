package com.simprints.fingerprint.connect.screens.alert

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.simprints.feature.alert.alertButton
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.screens.alert.AlertError.BLUETOOTH_NOT_ENABLED
import com.simprints.fingerprint.connect.screens.alert.AlertError.BLUETOOTH_NOT_SUPPORTED
import com.simprints.fingerprint.connect.screens.alert.AlertError.DISCONNECTED
import com.simprints.fingerprint.connect.screens.alert.AlertError.LOW_BATTERY
import com.simprints.fingerprint.connect.screens.alert.AlertError.MULTIPLE_PAIRED_SCANNERS
import com.simprints.fingerprint.connect.screens.alert.AlertError.NOT_PAIRED
import com.simprints.fingerprint.connect.screens.alert.AlertError.UNEXPECTED_ERROR
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
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
internal enum class AlertError(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val backgroundColor: AlertColor = AlertColor.Default,
    @DrawableRes val mainDrawable: Int,
    @DrawableRes val hintDrawable: Int? = null,
    val eventType: AlertScreenEventType,
    val leftButton: AlertButtonConfig,
    val rightButton: AlertButtonConfig? = null,
) {

    // Bluetooth errors
    BLUETOOTH_NOT_SUPPORTED(
        title = IDR.string.error_occurred_title,
        message = IDR.string.bluetooth_not_supported_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        eventType = AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED,
        leftButton = Buttons.closeButton(),
    ),

    BLUETOOTH_NOT_ENABLED(
        title = IDR.string.scanner_error_turn_scanner_on_title,
        message = IDR.string.bluetooth_not_enabled_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        eventType = AlertScreenEventType.BLUETOOTH_NOT_SUPPORTED,
        leftButton = Buttons.tryAgainButton(),
        rightButton = Buttons.bluetoothSettingsButton(),
    ),

    BLUETOOTH_NO_PERMISSION(
        title = IDR.string.no_permission_title,
        message = IDR.string.bluetooth_no_permission_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        eventType = AlertScreenEventType.BLUETOOTH_NO_PERMISSION,
        leftButton = Buttons.appSettingsButton(),
    ),

    NOT_PAIRED(
        title = IDR.string.bluetooth_error_pair_scanner_and_device_title,
        message = IDR.string.bluetooth_error_pair_scanner_and_device_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = IDR.drawable.ic_alert_hint_bt_disabled,
        eventType = AlertScreenEventType.NOT_PAIRED,
        leftButton = Buttons.pairScannerButton(),
    ),

    MULTIPLE_PAIRED_SCANNERS(
        title = IDR.string.scanner_error_turn_scanner_on_title,
        message = IDR.string.multiple_scanners_found_message,
        mainDrawable = IDR.drawable.ic_alert_bt,
        hintDrawable = R.drawable.multiple_scanners_found,
        eventType = AlertScreenEventType.MULTIPLE_PAIRED_SCANNERS,
        leftButton = Buttons.tryAgainButton(),
        rightButton = Buttons.bluetoothSettingsButton()
    ),

    // Scanner errors
    DISCONNECTED(
        title = IDR.string.scanner_error_turn_scanner_on_title,
        message = IDR.string.scanner_error_turn_scanner_on_message,
        backgroundColor = AlertColor.Default,
        mainDrawable = R.drawable.scanner_error_icon,
        eventType = AlertScreenEventType.DISCONNECTED,
        leftButton = Buttons.tryAgainButton(),
    ),

    LOW_BATTERY(
        title = IDR.string.low_battery_title,
        message = IDR.string.low_battery_message,
        backgroundColor = AlertColor.Default,
        mainDrawable = R.drawable.scanner_error_icon,
        hintDrawable = IDR.drawable.ic_alert_hint_battery,
        eventType = AlertScreenEventType.LOW_BATTERY,
        leftButton = Buttons.closeWithRefusalButton()
    ),

    //Unexpected errors
    UNEXPECTED_ERROR(
        title = IDR.string.error_occurred_title,
        message = IDR.string.unforeseen_error_message,
        backgroundColor = AlertColor.Red,
        mainDrawable = IDR.drawable.ic_alert_default,
        eventType = AlertScreenEventType.UNEXPECTED_ERROR,
        leftButton = Buttons.closeButton(),
    );

    private object Buttons {

        fun closeButton() = alertButton {
            textRes = IDR.string.close
            closeOnClick = true
            resultKey = ACTION_CLOSE
        }

        fun closeWithRefusalButton() = alertButton {
            textRes = IDR.string.close
            closeOnClick = true
            resultKey = ACTION_REFUSAL
        }

        fun bluetoothSettingsButton() = alertButton {
            textRes = IDR.string.settings_label
            closeOnClick = true
            resultKey = ACTION_BT_SETTINGS
        }

        fun appSettingsButton() = alertButton {
            textRes = IDR.string.settings_label
            closeOnClick = true
            resultKey = ACTION_APP_SETTINGS
        }

        fun tryAgainButton() = alertButton {
            textRes = IDR.string.try_again_label
            closeOnClick = true
            resultKey = ACTION_RETRY
        }

        fun pairScannerButton() = alertButton {
            textRes = IDR.string.pair_scanner_label
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
        eventType = this@AlertError.eventType

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

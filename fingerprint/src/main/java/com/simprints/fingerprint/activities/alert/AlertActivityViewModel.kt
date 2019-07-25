package com.simprints.fingerprint.activities.alert

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R

enum class AlertActivityViewModel(val type: Type,
                                  val leftButton: ButtonAction,
                                  val rightButton: ButtonAction,
                                  @StringRes val message: Int) {

    // Data errors
    GUID_NOT_FOUND_ONLINE(
        type = Type.DataError(
            title = R.string.verify_guid_not_found_title,
            hintDrawable = null
        ),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = R.string.verify_guid_not_found_online_message
    ),

    GUID_NOT_FOUND_OFFLINE(
        type = Type.DataError(
            title = R.string.verify_guid_not_found_title,
            hintDrawable = R.drawable.error_hint_wifi
        ),
        leftButton = ButtonAction.TryAgain,
        rightButton = ButtonAction.WifiSettings,
        message = R.string.verify_guid_not_found_offline_message
    ),

    // Bluetooth errors
    BLUETOOTH_NOT_SUPPORTED(
        type = Type.BluetoothError(
            backgroundColor = R.color.simprints_yellow,
            hintDrawable = R.drawable.bt_not_enabled
        ),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.BluetoothSettings,
        message = R.string.bluetooth_not_supported_message
    ),

    BLUETOOTH_NOT_ENABLED(
        type = Type.BluetoothError(
            hintDrawable = R.drawable.bt_not_enabled
        ),
        leftButton = ButtonAction.TryAgain,
        rightButton = ButtonAction.BluetoothSettings,
        message = R.string.bluetooth_not_enabled_message
    ),

    NOT_PAIRED(
        type = Type.BluetoothError(
            hintDrawable = R.drawable.bt_not_enabled
        ),
        leftButton = ButtonAction.TryAgain,
        rightButton = ButtonAction.BluetoothSettings,
        message = R.string.unbonded_scanner_message
    ),

    MULTIPLE_PAIRED_SCANNERS(
        type = Type.BluetoothError(
            hintDrawable = R.drawable.multiple_scanners_found
        ),
        leftButton = ButtonAction.TryAgain,
        rightButton = ButtonAction.BluetoothSettings,
        message = R.string.multiple_scanners_found_message
    ),

    // Scanner errors
    DISCONNECTED(
        type = Type.ScannerError(
            title = R.string.disconnected_title,
            hintDrawable = null
        ),
        leftButton = ButtonAction.TryAgain,
        rightButton = ButtonAction.BluetoothSettings,
        message = R.string.disconnected_message
    ),

    LOW_BATTERY(
        type = Type.ScannerError(
            title = R.string.low_battery_title,
            hintDrawable = R.drawable.low_battery_icon
        ),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = R.string.low_battery_message
    ),

    //Unexpected errors
    UNEXPECTED_ERROR(
        type = Type.UnexpectedError(),
        leftButton = ButtonAction.Close,
        rightButton = ButtonAction.None,
        message = R.string.unforeseen_error_message
    );

    companion object {
        fun fromAlertToAlertViewModel(alertType: FingerprintAlert): AlertActivityViewModel =
            when(alertType) {
                FingerprintAlert.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                FingerprintAlert.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
                FingerprintAlert.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                FingerprintAlert.BLUETOOTH_NOT_ENABLED -> BLUETOOTH_NOT_ENABLED
                FingerprintAlert.NOT_PAIRED -> NOT_PAIRED
                FingerprintAlert.MULTIPLE_PAIRED_SCANNERS -> MULTIPLE_PAIRED_SCANNERS
                FingerprintAlert.DISCONNECTED -> DISCONNECTED
                FingerprintAlert.LOW_BATTERY -> LOW_BATTERY
                FingerprintAlert.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
            }
    }

    @StringRes val title: Int = type.title
    @ColorRes val backgroundColor: Int = type.backgroundColor
    @DrawableRes val mainDrawable: Int = type.mainDrawable
    @DrawableRes val hintDrawable: Int? = type.hintDrawable

    sealed class Type(@StringRes val title: Int,
                      @ColorRes val backgroundColor: Int,
                      @DrawableRes val mainDrawable: Int,
                      @DrawableRes val hintDrawable: Int? = null) {

        class DataError(title: Int,
                        backgroundColor: Int = R.color.simprints_grey,
                        mainDrawable: Int = R.drawable.error_icon,
                        hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)

        class BluetoothError(title: Int = R.string.error_occurred_title,
                             backgroundColor: Int = R.color.simprints_blue,
                             mainDrawable: Int = R.drawable.bt_error_icon,
                             hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)

        class ScannerError(title: Int,
                           backgroundColor: Int = R.color.simprints_blue,
                           mainDrawable: Int = R.drawable.scanner_error_icon,
                           hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)

        class UnexpectedError(title: Int = R.string.error_occurred_title,
                              backgroundColor: Int = R.color.simprints_red,
                              mainDrawable: Int = R.drawable.error_icon,
                              hintDrawable: Int? = null)
            : Type(title, backgroundColor, mainDrawable, hintDrawable)
    }

    sealed class ButtonAction(@StringRes val buttonText: Int = R.string.empty) {
        object None : ButtonAction()
        object WifiSettings : ButtonAction(R.string.settings_label)
        object BluetoothSettings : ButtonAction(R.string.settings_label)
        object TryAgain : ButtonAction(R.string.try_again_label)
        object Close : ButtonAction(R.string.close)
    }
}

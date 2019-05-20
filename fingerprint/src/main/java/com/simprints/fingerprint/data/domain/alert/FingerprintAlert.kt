package com.simprints.fingerprint.data.domain.alert

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE
import com.simprints.fingerprint.data.domain.InternalConstants.ResultIntents.Companion.ALERT_TRY_AGAIN_RESULT
import com.simprints.id.domain.Constants

enum class FingerprintAlert(val type: Type,
                            val leftButton: ButtonAction,
                            val rightButton: ButtonAction,
                            @StringRes val message: Int) {

    // Data errors
    GUID_NOT_FOUND_ONLINE(
        type = Type.DataError(
            title = R.string.verify_guid_not_found_title,
            hintDrawable = null
        ),
        leftButton = ButtonAction.Close(resultCode = SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE),
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
        message = R.string.verify_guid_not_found_online_message
    ),

    // Bluetooth errors

    BLUETOOTH_NOT_SUPPORTED(
        type = Type.BluetoothError(
            backgroundColor = R.color.simprints_yellow,
            hintDrawable = R.drawable.bt_not_enabled
        ),
        leftButton = ButtonAction.Close(resultCode = Constants.SIMPRINTS_CANCELLED),
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
        leftButton = ButtonAction.Close(resultCode = Constants.SIMPRINTS_CANCELLED),
        rightButton = ButtonAction.None,
        message = R.string.low_battery_message
    ),

    //Unexpected errors

    UNEXPECTED_ERROR(
        type = Type.UnexpectedError(),
        leftButton = ButtonAction.Close(resultCode = Constants.SIMPRINTS_CANCELLED),
        rightButton = ButtonAction.None,
        message = R.string.unforeseen_error_message
    );

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

    sealed class ButtonAction(@StringRes val buttonText: Int = R.string.empty,
                              val resultCode: Int? = null) {
        object None : ButtonAction()
        object WifiSettings : ButtonAction(R.string.settings_label)
        object BluetoothSettings : ButtonAction(R.string.settings_label)
        object TryAgain : ButtonAction(R.string.try_again_label, ALERT_TRY_AGAIN_RESULT)
        class Close(resultCode: Int) : ButtonAction(R.string.close, resultCode)
    }
}

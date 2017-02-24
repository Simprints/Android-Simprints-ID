package com.simprints.id.activities;

import android.graphics.Color;

import com.simprints.id.R;

public enum ALERT_TYPE {

    //Configuration errors
    INVALID_API_KEY(R.string.configuration_error_title, R.string.invalid_apikey_message,
            R.drawable.error_icon, R.drawable.error_hint_key, R.string.close, R.string.empty, false, R.color.simprints_red),

    MISSING_API_KEY(R.string.configuration_error_title, R.string.missing_apikey_message,
            R.drawable.error_icon, R.drawable.error_hint_key, R.string.close, R.string.empty, false, R.color.simprints_red),

    MISSING_USER_ID(R.string.configuration_error_title, R.string.missing_userId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty, false, R.color.simprints_red),

    MISSING_MODULE_ID(R.string.configuration_error_title, R.string.missing_moduleId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty, false, R.color.simprints_red),

    MISSING_UPDATE_GUID(R.string.configuration_error_title, R.string.missing_updateId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty, false, R.color.simprints_red),

    MISSING_VERIFY_GUID(R.string.configuration_error_title, R.string.missing_verifyId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty, false, R.color.simprints_red),

    //Verification errors
    UNVERIFIED_API_KEY(R.string.alert_api_key_verify_failed, R.string.alert_api_key_verify_failed_body,
            R.drawable.error_icon, R.drawable.error_hint_wifi, R.string.try_again_label, R.string.close, true, R.color.simprints_grey),

    //Bluetooth errors
    BLUETOOTH_NOT_SUPPORTED(R.string.bluetooth_error_title, R.string.bluetooth_not_supported_message,
            R.drawable.bt_error_icon, R.drawable.bt_not_enabled,
            R.string.try_again_label, R.string.settings_label, false, R.color.simprints_red),

    BLUETOOTH_NOT_ENABLED(R.string.bluetooth_error_title, R.string.bluetooth_not_enabled_message,
            R.drawable.bt_error_icon, R.drawable.bt_not_enabled,
            R.string.try_again_label, R.string.settings_label, true, R.color.simprints_blue),
    NOT_PAIRED(R.string.bluetooth_error_title, R.string.unbonded_scanner_message,
            R.drawable.bt_error_icon, R.drawable.scanner_error_icon,
            R.string.try_again_label, R.string.settings_label, true, R.color.simprints_blue),
    MULTIPLE_PAIRED_SCANNERS(R.string.bluetooth_error_title, R.string.multiple_scanners_found_message,
            R.drawable.bt_error_icon, R.drawable.multiple_scanners_found,
            R.string.try_again_label, R.string.settings_label, true, R.color.simprints_blue),

    //Scanner connection errors
    DISCONNECTED(R.string.disconnected_title, R.string.disconnected_message,
            R.drawable.scanner_error_icon, -1, R.string.try_again_label, R.string.settings_label, true, R.color.simprints_blue),

    //Unexpected errors
    UNEXPECTED_ERROR(R.string.error_occurred_title, R.string.unforeseen_error_message,
            R.drawable.error_icon, -1, R.string.try_again_label, R.string.close, true, R.color.simprints_blue);

    private int alertTitleId;
    private int alertMessageId;
    private int alertMainDrawableId;
    private int alertHintDrawableId;
    private int alertLeftButtonTextId;
    private int alertRightButtonTextId;
    private boolean mustBeLogged;
    private int backgroundColor;

    ALERT_TYPE(int alertTitleId, int alertMessageId, int alertDrawableId, int alertHintDrawableId,
               int alertLeftButtonTextId, int alertRightButtonTextId,
               boolean mustBeLogged, int backgroundColor) {
        this.alertTitleId = alertTitleId;
        this.alertMessageId = alertMessageId;
        this.alertMainDrawableId = alertDrawableId;
        this.alertHintDrawableId = alertHintDrawableId;
        this.alertLeftButtonTextId = alertLeftButtonTextId;
        this.alertRightButtonTextId = alertRightButtonTextId;
        this.mustBeLogged = mustBeLogged;
        this.backgroundColor = backgroundColor;
    }

    public int getAlertTitleId() {
        return alertTitleId;
    }

    public int getAlertMessageId() {
        return alertMessageId;
    }

    public int getAlertMainDrawableId() {
        return alertMainDrawableId;
    }

    public boolean isLeftButtonActive() {
        return alertLeftButtonTextId != R.string.empty;
    }

    public boolean isRightButtonActive() {
        return alertRightButtonTextId != R.string.empty;
    }

    public int getAlertLeftButtonTextId() {
        return alertLeftButtonTextId;
    }

    public int getAlertRightButtonTextId() {
        return alertRightButtonTextId;
    }

    public boolean mustBeLogged() {
        return mustBeLogged;
    }

    public int getAlertHintDrawableId() {
        return alertHintDrawableId;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setAlertHintDrawableId(int alertHintDrawableId) {
        this.alertHintDrawableId = alertHintDrawableId;
    }
}

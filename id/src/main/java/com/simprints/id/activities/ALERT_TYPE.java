package com.simprints.id.activities;

import com.simprints.id.R;

public enum ALERT_TYPE {

    //Configuration errors
    INVALID_API_KEY(R.string.configuration_error_title, R.string.invalid_apikey_message,
            R.drawable.error_icon, R.drawable.error_hint_key, R.string.close, R.string.empty, false),

    //Bluetooth errors
    BLUETOOTH_NOT_SUPPORTED(R.string.bluetooth_error_title, R.string.bluetooth_not_supported_message,
            R.drawable.bt_error_icon, R.drawable.bluetooth_not_enabled,
            R.string.try_again_label, R.string.settings_label, false),
    BLUETOOTH_NOT_ENABLED(R.string.bluetooth_error_title, R.string.bluetooth_not_enabled_message,
            R.drawable.bt_error_icon, R.drawable.bluetooth_not_enabled,
            R.string.try_again_label, R.string.settings_label, true),
    NOT_PAIRED(R.string.bluetooth_error_title, R.string.unbonded_scanner_message,
            R.drawable.bt_error_icon, R.drawable.scanner_error_icon,
            R.string.try_again_label, R.string.settings_label, true),
    MULTIPLE_PAIRED_SCANNERS(R.string.bluetooth_error_title, R.string.multiple_scanners_found_message,
            R.drawable.bt_error_icon, R.drawable.multiple_scanners_found,
            R.string.try_again_label, R.string.settings_label, true),

    //Scanner connection errors
    DISCONNECTED(R.string.disconnected_title, R.string.disconnected_message,
            R.drawable.scanner_error_icon, -1, R.string.try_again_label, R.string.settings_label, true),

    //Unexpected errors
    UNEXPECTED_ERROR(R.string.error_occurred_title, R.string.unforeseen_error_message,
            R.drawable.error_icon, -1, R.string.try_again_label, R.string.close, true);

    private int alertTitleId;
    private int alertMessageId;
    private int alertMainDrawableId;
    private int alertHintDrawableId;
    private int alertLeftButtonTextId;
    private int alertRightButtonTextId;
    private boolean mustBeLogged;

    ALERT_TYPE(int alertTitleId, int alertMessageId, int alertDrawableId, int alertHintDrawableId,
               int alertLeftButtonTextId, int alertRightButtonTextId,
               boolean mustBeLogged) {
        this.alertTitleId = alertTitleId;
        this.alertMessageId = alertMessageId;
        this.alertMainDrawableId = alertDrawableId;
        this.alertHintDrawableId = alertHintDrawableId;
        this.alertLeftButtonTextId = alertLeftButtonTextId;
        this.alertRightButtonTextId = alertRightButtonTextId;
        this.mustBeLogged = mustBeLogged;
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

    public void setAlertHintDrawableId(int alertHintDrawableId) {
        this.alertHintDrawableId = alertHintDrawableId;
    }
}

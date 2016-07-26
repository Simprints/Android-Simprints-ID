package com.simprints.id.activities;

import com.simprints.id.R;

public enum ALERT_TYPE {

    MISSING_API_KEY(R.string.configuration_error_title, R.string.missing_apikey_message,
            R.drawable.configuration_error, R.string.close, -1),
    INVALID_API_KEY(R.string.configuration_error_title, R.string.invalid_apikey_message,
            R.drawable.configuration_error, R.string.close, -1),

    BLUETOOTH_NOT_SUPPORTED(R.string.bluetooth_not_supported_title, R.string.bluetooth_not_supported_message,
            R.drawable.bluetooth_not_supported, R.string.close, -1),
    BLUETOOTH_NOT_ENABLED(R.string.bluetooth_not_enabled_title, R.string.bluetooth_not_enabled_message,
            R.drawable.bluetooth_not_enabled, R.string.try_again_label, R.string.settings_label),
    BLUETOOTH_UNBONDED_SCANNER(R.string.unbonded_scanner_title, R.string.unbonded_scanner_message,
            R.drawable.bluetooth_not_enabled, R.string.try_again_label, R.string.settings_label),

    NO_PAIRED_SCANNER(R.string.no_scanner_found_title, R.string.no_scanner_found_message,
            R.drawable.no_scanner_found, R.string.try_again_label, R.string.settings_label),
    MULTIPLE_PAIRED_SCANNERS(R.string.multiple_scanners_found_title, R.string.multiple_scanners_found_message,
            R.drawable.multiple_scanners_found, R.string.try_again_label, R.string.settings_label),

    NETWORK_FAILURE(R.string.network_failure_title, R.string.network_failure_message,
            R.drawable.generic_failure, R.string.try_again_label, -1),

    UNEXPECTED_ERROR(R.string.error_occured_title, R.string.unforeseen_error_message,
            R.drawable.generic_failure, R.string.close, -1);

    private int alertTitleId;
    private int alertMessageId;
    private int alertDrawableId;
    private int alertLeftButtonTextId;
    private int alertRightButtonTextId;

    ALERT_TYPE(int alertTitleId, int alertMessageId, int alertDrawableId, int alertLeftButtonTextId, int alertRightButtonTextId) {
        this.alertTitleId = alertTitleId;
        this.alertMessageId = alertMessageId;
        this.alertDrawableId = alertDrawableId;
        this.alertLeftButtonTextId = alertLeftButtonTextId;
        this.alertRightButtonTextId = alertRightButtonTextId;
    }

    public int getAlertTitleId() {
        return alertTitleId;
    }

    public int getAlertMessageId() {
        return alertMessageId;
    }

    public int getAlertDrawableId() {
        return alertDrawableId;
    }

    public boolean isLeftButtonVisible() {
        return alertLeftButtonTextId != -1;
    }

    public boolean isRightButtonVisible() {
        return alertRightButtonTextId != -1;
    }


    public int getAlertLeftButtonTextId() {
        return alertLeftButtonTextId;
    }

    public int getAlertRightButtonTextId() {
        return alertRightButtonTextId;
    }
}

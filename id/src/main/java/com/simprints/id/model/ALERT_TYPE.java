package com.simprints.id.model;

import com.simprints.id.R;
import com.simprints.id.tools.InternalConstants;
import com.simprints.libsimprints.Constants;

import static android.app.Activity.RESULT_CANCELED;

public enum ALERT_TYPE {

    //Configuration errors

    INVALID_API_KEY(R.string.configuration_error_title, R.string.invalid_apikey_message,
            R.drawable.error_icon, R.drawable.error_hint_key, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_API_KEY),

    INVALID_PROJECT_ID(R.string.configuration_error_title, R.string.invalid_projectId_message,
        R.drawable.error_icon, R.drawable.error_hint_key, R.string.close, R.string.empty,
        false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_PROJECT_ID),


    MISSING_PROJECT_ID_OR_API_KEY(R.string.configuration_error_title, R.string.missing_projectId_or_apiKey_message,
        R.drawable.error_icon, R.drawable.error_hint_key, R.string.close, R.string.empty,
        false, R.color.simprints_alert_orange, Constants.SIMPRINTS_MISSING_PROJECT_ID_OR_API_KEY),

    INVALID_INTENT_ACTION(R.string.configuration_error_title, R.string.invalid_intentAction_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_METADATA),

    INVALID_METADATA(R.string.configuration_error_title, R.string.invalid_metadata_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_INTENT_ACTION),

    MISSING_USER_ID(R.string.configuration_error_title, R.string.missing_userId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_MISSING_USER_ID),

    INVALID_USER_ID(R.string.configuration_error_title, R.string.invalid_userId_message,
        R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
        false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_USER_ID),


    MISSING_MODULE_ID(R.string.configuration_error_title, R.string.missing_moduleId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_MISSING_MODULE_ID),

    INVALID_MODULE_ID(R.string.configuration_error_title, R.string.invalid_moduleId_message,
        R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
        false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_MODULE_ID),

    MISSING_UPDATE_GUID(R.string.configuration_error_title, R.string.missing_updateId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_MISSING_UPDATE_GUID),

    INVALID_UPDATE_GUID(R.string.configuration_error_title, R.string.invalid_updateId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_UPDATE_GUID),

    MISSING_VERIFY_GUID(R.string.configuration_error_title, R.string.missing_verifyId_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_MISSING_VERIFY_GUID),

    INVALID_VERIFY_GUID(R.string.configuration_error_title, R.string.invalid_verifyId_message,
        R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
        false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_VERIFY_GUID),

    INVALID_RESULT_FORMAT(R.string.configuration_error_title, R.string.invalid_resultFormat_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_RESULT_FORMAT),

    INVALID_CALLING_PACKAGE(R.string.configuration_error_title, R.string.invalid_callingPackage_message,
        R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
        false, R.color.simprints_alert_orange, Constants.SIMPRINTS_INVALID_CALLING_PACKAGE),


    // TODO: add Unexpected Parameter return code to LibSimprints.
    UNEXPECTED_PARAMETER(R.string.configuration_error_title, R.string.unexpected_parameter_message,
            R.drawable.error_icon, R.drawable.error_hint_cog, R.string.close, R.string.empty,
            false, R.color.simprints_alert_orange, Constants.SIMPRINTS_CANCELLED),

    // Verification errors

    UNVERIFIED_API_KEY(R.string.alert_api_key_verify_failed, R.string.alert_api_key_verify_failed_body,
            R.drawable.error_icon, R.drawable.error_hint_wifi, R.string.try_again_label, R.string.settings_label,
            true, R.color.simprints_grey, InternalConstants.RESULT_TRY_AGAIN),

    // Data errors

    GUID_NOT_FOUND_ONLINE(R.string.verify_guid_not_found_title, R.string.verify_guid_not_found_online_message,
            R.drawable.error_icon, -1, R.string.close, R.string.empty,
            true, R.color.simprints_grey, Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_ONLINE),

    GUID_NOT_FOUND_OFFLINE(R.string.verify_guid_not_found_title, R.string.verify_guid_not_found_offline_message,
            R.drawable.error_icon, R.drawable.error_hint_wifi, R.string.try_again_label, R.string.settings_label,
            true, R.color.simprints_grey, Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_OFFLINE),

    // Bluetooth errors

    BLUETOOTH_NOT_SUPPORTED(R.string.bluetooth_error_title, R.string.bluetooth_not_supported_message,
            R.drawable.bt_error_icon, R.drawable.bt_not_enabled, R.string.try_again_label, R.string.settings_label,
            false, R.color.simprints_alert_orange, RESULT_CANCELED),

    BLUETOOTH_NOT_ENABLED(R.string.bluetooth_error_title, R.string.bluetooth_not_enabled_message,
            R.drawable.bt_error_icon, R.drawable.bt_not_enabled, R.string.try_again_label, R.string.settings_label,
            true, R.color.simprints_blue, InternalConstants.RESULT_TRY_AGAIN),

    NOT_PAIRED(R.string.bluetooth_error_title, R.string.unbonded_scanner_message,
            R.drawable.bt_error_icon, R.drawable.scanner_error_icon, R.string.try_again_label, R.string.settings_label,
            true, R.color.simprints_blue, InternalConstants.RESULT_TRY_AGAIN),

    MULTIPLE_PAIRED_SCANNERS(R.string.bluetooth_error_title, R.string.multiple_scanners_found_message,
            R.drawable.bt_error_icon, R.drawable.multiple_scanners_found, R.string.try_again_label, R.string.settings_label,
            true, R.color.simprints_blue, InternalConstants.RESULT_TRY_AGAIN),

    // Scanner errors

    DISCONNECTED(R.string.disconnected_title, R.string.disconnected_message,
            R.drawable.scanner_error_icon, -1, R.string.try_again_label, R.string.settings_label,
            true, R.color.simprints_blue, InternalConstants.RESULT_TRY_AGAIN),

    LOW_BATTERY(R.string.low_battery_title, R.string.low_battery_message,
            R.drawable.scanner_error_icon, R.drawable.low_battery_icon, R.string.close, R.string.empty,
            true, R.color.simprints_blue, Constants.SIMPRINTS_MISSING_UPDATE_GUID),

    //Unexpected errors

    UNEXPECTED_ERROR(R.string.error_occurred_title, R.string.unforeseen_error_message,
            R.drawable.error_icon, -1, R.string.try_again_label, R.string.close,
            true, R.color.simprints_red, InternalConstants.RESULT_TRY_AGAIN);

    private int alertTitleId;
    private int alertMessageId;
    private int alertMainDrawableId;
    private int alertHintDrawableId;
    private int alertLeftButtonTextId;
    private int alertRightButtonTextId;
    private boolean mustBeLogged;
    private int backgroundColor;
    private int resultCode;

    ALERT_TYPE(int alertTitleId, int alertMessageId, int alertDrawableId, int alertHintDrawableId,
               int alertLeftButtonTextId, int alertRightButtonTextId,
               boolean mustBeLogged, int backgroundColor, int resultCode) {
        this.alertTitleId = alertTitleId;
        this.alertMessageId = alertMessageId;
        this.alertMainDrawableId = alertDrawableId;
        this.alertHintDrawableId = alertHintDrawableId;
        this.alertLeftButtonTextId = alertLeftButtonTextId;
        this.alertRightButtonTextId = alertRightButtonTextId;
        this.mustBeLogged = mustBeLogged;
        this.backgroundColor = backgroundColor;
        this.resultCode = resultCode;
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

    public int getResultCode() {
        return resultCode;
    }
}

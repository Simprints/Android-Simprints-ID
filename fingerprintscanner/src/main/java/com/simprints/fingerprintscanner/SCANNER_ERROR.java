package com.simprints.fingerprintscanner;

public enum SCANNER_ERROR {

    BUSY("Cannot perform request because the scanner is busy."),
    INVALID_STATE("Operation failed because the scanner is in an invalid state (already connected, not connected, already disconnected, ...)."),
    IO_ERROR("Operation failed because of an IO error."),
    NO_RESPONSE("The scanner is not answering."),
    UNEXPECTED("Unexpected error, should not happen"),
    OFF("Operation failed because the scanner is off."),

    BLUETOOTH_DISABLED("Connection failed because phone's bluetooth is disabled."),
    BLUETOOTH_NOT_SUPPORTED("Connection failed because the phone does not support bluetooth."),
    SCANNER_UNBONDED("Connection failed because the scanner is not bonded to the phone."),
    SCANNER_UNREACHABLE("Connection failed because the scanner is unreachable"),

    UN20_FAILURE("UN20 wake up failed for abnormal reasons, SHOULD NOT HAPPEN."),
    UN20_INVALID_STATE("UN20 operation failed because it is in an invalid state (awaken, shutdown, waking up, shuttingdown, depending on the operation."),
    UN20_SDK_ERROR("UN20 operation failed because of an internal error."),
    UN20_LOW_VOLTAGE("UN20 operation failed because the battery voltage is too low."),

    OUTDATED_SCANNER_INFO("Update scanner info before starting continuous capture"),
    INTERRUPTED("Operation failed because it was interrupted."),
    TIMEOUT("Operation failed because it timed out.");


    private String details;

    SCANNER_ERROR(String details) {
        this.details = details;
    }

    public String details() {
        return details;
    }
}
//
//    TRIGGER_PRESSED("Trigger pressed"),
//
//    SET_SENSOR_CONFIG_SUCCESS("Sensor configuration was successfully set"),
//    SET_SENSOR_CONFIG_FAILURE("Setting sensor configuration failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    SET_UI_SUCCESS("UI was successfully set"),
//    SET_UI_FAILURE("Setting UI failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    PAIR_SUCCESS("Paired successfully"),
//    PAIR_FAILURE("Pairing failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    CAPTURE_IMAGE_SUCCESS("Image captured successfully"),
//    CAPTURE_IMAGE_SDK_ERROR("Image capture failed because of an error in UN20 SDK"),
//    CAPTURE_IMAGE_WRONG( "Image capture failed because image is not a real fingerprint image"),
//    CAPTURE_IMAGE_INVALID_PARAM( "Image capture failed because invalid parameter used"),
//    CAPTURE_IMAGE_LINE_DROPPED("Image capture failed because data lost"),
//    CAPTURE_IMAGE_INVALID_STATE("Image capture failed because the un20 is not awaken"),
//    CAPTURE_IMAGE_FAILURE("Image capture failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    CONTINUOUS_CAPTURE_STARTED("Continuous capture started"),
//    CONTINUOUS_CAPTURE_SUCCESS("Continuous capture successful"),
//    CONTINUOUS_CAPTURE_ERROR("Error during continuous capture"),
//    CONTINUOUS_CAPTURE_STOPPED("Continuous capture stopped"),
//
//    EXTRACT_IMAGE_SUCCESS("Image extracted successfully"),
//    EXTRACT_IMAGE_IO_ERROR("Image extraction failed because of an IO error"),
//    EXTRACT_IMAGE_NO_IMAGE("Image extraction failed because there is no image available"),
//    EXTRACT_IMAGE_FAILURE("Image extraction failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    EXTRACT_IMAGE_QUALITY_SUCCESS("Image quality extracted successfully"),
//    EXTRACT_IMAGE_QUALITY_NO_IMAGE("Image quality extraction failed because there is no image available"),
//    EXTRACT_IMAGE_QUALITY_SDK_ERROR("Image quality extraction failed because of an error in UN20 SDK"),
//    EXTRACT_IMAGE_QUALITY_FAILURE("Image quality extraction failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    GENERATE_TEMPLATE_SUCCESS("Template generated successfully"),
//    GENERATE_TEMPLATE_NO_IMAGE("Template generation failed because there is no image available"),
//    GENERATE_TEMPLATE_NO_QUALITY("Template generation failed because there is no image quality available"),
//    GENERATE_TEMPLATE_LOW_FEAT_NUMBER("Template generation failed because of inadequate number of features"),
//    GENERATE_TEMPLATE_INVALID_TYPE("Template generation failed because the template is an invalid type"),
//    GENERATE_TEMPLATE_EXTRACT_FAIL("Template generation failed because of a SDK extraction failure"),
//    GENERATE_TEMPLATE_SDK_ERROR("Template generation failed because of an unspecified error in UN20 SDK"),
//    GENERATE_TEMPLATE_FAILURE("Template generation failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    EXTRACT_TEMPLATE_SUCCESS("Template extracted successfully"),
//    EXTRACT_TEMPLATE_NO_TEMPLATE("Template extraction failed because there is no template available"),
//    EXTRACT_TEMPLATE_IO_ERROR("Template extraction failed because of an IO error"),
//    EXTRACT_TEMPLATE_FAILURE("Template extraction failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    UN20_CANNOT_CHECK_STATE("Abnormal error checking the state of the UN20, SHOULD NOT HAPPEN"),
//
//    UN20_SHUTTING_DOWN("UN20 started shutting down"),
//    UN20_SHUTDOWN_SUCCESS("UN20 shut down successfully"),
//    UN20_SHUTDOWN_INVALID_STATE("UN20 shut down failed because it is already shut / waking up or down"),
//    UN20_SHUTDOWN_FAILURE("UN20 shut down failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    UN20_WAKING_UP("UN20 started waking up"),
//    UN20_WAKEUP_SUCCESS("UN20 woken up successfully"),
//    UN20_WAKEUP_INVALID_STATE("UN20 wake up failed because it is already woken up / waking up or down"),
//
//    EXTRACT_CRASH_LOG_SUCCESS("Crash log extracted successfully"),
//    EXTRACT_CRASH_LOG_NO_CRASHLOG("Crash log extraction failed because there is no crash log available"),
//    EXTRACT_CRASH_LOG_FAILURE("Crash log extraction failed for abnormal reasons, SHOULD NOT HAPPEN"),
//
//    SET_HARDWARE_CONFIG_SUCCESS("Hardware configuration was successfully set"),
//    SET_HARDWARE_CONFIG_INVALID_STATE("Hardware configuration failed because UN20 is not shutdown"),
//    SET_HARDWARE_CONFIG_INVALID_CONFIG("Hardware configuration failed because an invalid config was specified"),
//    SET_HARDWARE_CONFIG_FAILURE("Hardware configuration failed for abnormal reasons, SHOULD NOT HAPPEN");

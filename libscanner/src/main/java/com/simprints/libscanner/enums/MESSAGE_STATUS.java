package com.simprints.libscanner.enums;

public enum MESSAGE_STATUS {

    GOOD("No error"),
    ERROR("Non-specific error"),
    UN20_STATE_ERROR("UN20 is in the wrong state for command"),
    UNSUPPORTED("Message is unsupported,"),
    NO_IMAGE("No current image to operate on"),
    NO_QUALITY("No current quality value for current image"),
    NO_TEMPLATE("No current template to operate on"),
    SAVE_ERROR("Unable to save image"),
    SDK_ERROR("Error in UN20 SDK caused operation to fail"),
    NO_CRASH_LOG("no crash log data available"),
    BAD_PARAMETER("parameter specified is not valid"),
    UN20_VOLTAGE("battery voltage is too low to start UN20"),
    CHARGING("unit is in charge mode, commands not accepted"),
    SDK_ERROR_CODE("Secugen error code"),
    UNKNOWN("Unknown status");

    private final static MESSAGE_STATUS allValues[] = MESSAGE_STATUS.values();

    public static MESSAGE_STATUS fromId(int id) {
        if (id < 0 || id > MESSAGE_TYPE.UNKNOWN.ordinal())
            return UNKNOWN;
        else
            return allValues[id];
    }

    private String details;

    MESSAGE_STATUS(String details) {
        this.details = details;
    }

    public String details() {
        return details;
    }


}
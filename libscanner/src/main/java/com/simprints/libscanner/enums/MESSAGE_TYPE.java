package com.simprints.libscanner.enums;

/**
 * nb: these values must match those in firmware msg_format.h
 */
public enum MESSAGE_TYPE {
    GET_SENSOR_INFO,
    SET_SENSOR_CONFIG,
    SET_UI,
    PAIR,
    REPORT_UI,
    CAPTURE_IMAGE,
    CAPTURE_PROGRESS,
    CAPTURE_ABORT,
    RECOVER_IMAGE,
    IMAGE_FRAGMENT,
    STORE_IMAGE,
    IMAGE_QUALITY,
    GENERATE_TEMPLATE,
    RECOVER_TEMPLATE,
    COMPARE_TEMPLATE,
    UN20_SHUTDOWN,
    UN20_WAKEUP,
    UN20_WAKINGUP,
    UN20_READY,
    UN20_ISSHUTDOWN,
    UN20_GET_INFO,
    GET_IMAGE_FRAGMENT,
    GET_TEMPLATE_FRAGMENT,
    UN_20_SHUTDOWN_NO_ACK,
    GET_CRASH_LOG,
    SET_HARDWARE_CONFIG,
    DISABLE_FINGER_CHECK,
    ENABLE_FINGER_CHECK,
    CONNECT_TO_SCANNER,
    DISCONNECT_FROM_SCANNER,
    NONE,
    UNKNOWN;

    private final static MESSAGE_TYPE allValues[] = MESSAGE_TYPE.values();

    public static MESSAGE_TYPE fromId(int id) {
        if (id < 0 || id > MESSAGE_TYPE.UNKNOWN.ordinal())
            return UNKNOWN;
        else
            return allValues[id];
    }
}



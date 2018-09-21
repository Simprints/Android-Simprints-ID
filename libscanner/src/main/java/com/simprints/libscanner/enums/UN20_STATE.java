package com.simprints.libscanner.enums;


public enum UN20_STATE {

    SHUTDOWN,      // un20 is powered off
    STARTING_UP,   // un20 has powered on, waiting for connection
    READY,         // un20 is powered up and ready
    SHUTTING_DOWN, // un20 is shutting down
    UNKNOWN;        // un20 unknown status

    private final static UN20_STATE allValues[] = UN20_STATE.values();

    public static UN20_STATE fromId(int id) {
        if (id < 0 || MESSAGE_TYPE.UNKNOWN.ordinal() < id)
            return UNKNOWN;
        else
            return allValues[id];
    }
}

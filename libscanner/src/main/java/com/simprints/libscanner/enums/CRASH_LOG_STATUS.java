package com.simprints.libscanner.enums;

public enum CRASH_LOG_STATUS {
    HW_HARD_FAULT, // processor fault
    HW_UNEXPECTED_INTERRUPT, // unused interrupt
    SW_RTOS_STACK, // stack overflow
    SW_RTOS_MALLOC, // out of heap error
    SW_RTOS_ASSERT, // internal assert
    SW_THREAD_STUCK, // watchdog task found zombie task
    SW_ASSERT, // assert statment in the code (string)
    SW_ERROR, // as above
    SW_ABORT, // code has called about
    SW_CPP_PURE_VIRTUAL, // cpp virual method
    RESET, // watchdog gone off
    REQUESTED, // app requested restart
    UNKNOWN; // unknown

    private final static CRASH_LOG_STATUS allValues[] = CRASH_LOG_STATUS.values();

    public static CRASH_LOG_STATUS fromId(int id) {
        if (id < 0 || CRASH_LOG_STATUS.UNKNOWN.ordinal() < id)
            return UNKNOWN;
        else
            return allValues[id];
    }
}

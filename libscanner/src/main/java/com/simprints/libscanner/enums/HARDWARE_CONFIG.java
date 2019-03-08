package com.simprints.libscanner.enums;

public enum HARDWARE_CONFIG {
    NORMAL,            // Return configuration to operational state
    UN20_BOOTLOADER,   // Configure for and invoke UN20 bootloader
    UN20_FTP,          // Configure for and invoke UN20 IP over USB services
    LPC_BOOTLOADER,     // Configure for and invoke LPC bootloader
    UNKNOWN;

    private final static HARDWARE_CONFIG allValues[] = HARDWARE_CONFIG.values();

    public static HARDWARE_CONFIG fromId(int id) {
        if (id < 0 || HARDWARE_CONFIG.UNKNOWN.ordinal() < id)
            return UNKNOWN;
        else
            return allValues[id];
    }
}

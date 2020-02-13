package com.simprints.fingerprintscanner.v2.domain.root

import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException

enum class RootMessageType(val byte: Byte) {
    ENTER_MAIN_MODE(0x10),
    ENTER_CYPRESS_OTA_MODE(0x20),
    ENTER_STM_OTA_MODE(0x30),
    GET_VERSION(0xD0.toByte()),
    SET_VERSION(0xE0.toByte());

    companion object {
        fun fromByte(byte: Byte) = values().find { it.byte == byte }
            ?: throw InvalidMessageException("Invalid RootMessageType received with bytes: $byte")
    }
}

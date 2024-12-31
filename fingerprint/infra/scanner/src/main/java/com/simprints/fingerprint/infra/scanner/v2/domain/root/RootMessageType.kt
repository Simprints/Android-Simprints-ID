package com.simprints.fingerprint.infra.scanner.v2.domain.root

import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException

enum class RootMessageType(
    val byte: Byte,
) {
    ENTER_MAIN_MODE(0x10),
    ENTER_CYPRESS_OTA_MODE(0x20),
    ENTER_STM_OTA_MODE(0x30),
    GET_CYPRESS_VERSION(0xC0.toByte()),
    GET_VERSION(0xD0.toByte()),
    SET_VERSION(0xE0.toByte()),

    // extended api
    GET_EXTENDED_VERSION(0xD1.toByte()),
    GET_HARDWARE_VERSION(0xF0.toByte()),
    GET_CYPRESS_EXTENDED_VERSION(0xC1.toByte()),
    SET_EXTENDED_VERSION(0xE1.toByte()),
    ;

    companion object {
        fun fromByte(byte: Byte) = RootMessageType.entries.find { it.byte == byte }
            ?: throw InvalidMessageException("Invalid RootMessageType received with bytes: $byte")
    }
}

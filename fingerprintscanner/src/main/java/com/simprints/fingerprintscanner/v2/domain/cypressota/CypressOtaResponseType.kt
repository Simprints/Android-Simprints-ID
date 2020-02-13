package com.simprints.fingerprintscanner.v2.domain.cypressota

enum class CypressOtaResponseType(val byte: Byte? = null) {
    OK(0x30),
    CONTINUE(0x39),
    ERROR; // Error is equivalent to receiving any other bytes

    companion object {
        fun fromByte(byte: Byte) = values().find { it.byte == byte } ?: ERROR
    }
}

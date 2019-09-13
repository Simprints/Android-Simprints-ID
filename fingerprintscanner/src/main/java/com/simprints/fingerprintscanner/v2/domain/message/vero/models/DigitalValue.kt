package com.simprints.fingerprintscanner.v2.domain.message.vero.models

enum class DigitalValue(val byte: Byte) {

    FALSE(0x00),
    TRUE(0xFF.toByte());

    companion object {
        fun fromByte(byte: Byte) = values().find { it.byte == byte } ?: TODO()
    }
}

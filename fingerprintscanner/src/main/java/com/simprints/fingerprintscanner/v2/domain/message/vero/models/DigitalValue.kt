package com.simprints.fingerprintscanner.v2.domain.message.vero.models

enum class DigitalValue(val byte: Byte) {

    FALSE(0x00),
    TRUE(0xFF.toByte());

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] } ?: TODO("exception handling")
    }
}

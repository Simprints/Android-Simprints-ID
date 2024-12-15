package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models

enum class DigitalValue(
    val byte: Byte,
) {
    FALSE(0x00),
    TRUE(0xFF.toByte()),
    ;

    companion object {
        fun fromBytes(bytes: ByteArray) = if (bytes[0] == 0x00.toByte()) FALSE else TRUE
    }
}

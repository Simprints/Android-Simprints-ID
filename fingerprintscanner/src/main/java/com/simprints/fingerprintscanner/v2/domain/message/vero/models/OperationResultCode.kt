package com.simprints.fingerprintscanner.v2.domain.message.vero.models

enum class OperationResultCode(val byte: Byte) {

    OK(0x00),
    UNKNOWN_ERROR(0xFF.toByte());

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] } ?: TODO("exception handling")
    }
}

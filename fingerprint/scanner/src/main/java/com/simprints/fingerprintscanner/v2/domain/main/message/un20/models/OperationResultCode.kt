package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString

enum class OperationResultCode(val byte: Byte) {

    OK(0x00),
    UNKNOWN_ERROR(0xFF.toByte());

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] }
            ?: throw InvalidMessageException("Invalid OperationResultCode received with bytes: ${bytes.toHexString()}")
    }
}

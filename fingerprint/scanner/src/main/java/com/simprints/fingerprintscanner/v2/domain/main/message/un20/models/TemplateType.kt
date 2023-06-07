package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString

enum class TemplateType(val byte: Byte) {
    ISO_19794_2_2011(0x10);

    fun getBytes() = byteArrayOf(byte)

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] }
            ?: throw InvalidMessageException("Invalid TemplateType received with bytes: ${bytes.toHexString()}")
    }
}

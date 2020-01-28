package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString

enum class ImageFormat(val byte: Byte) {
    RAW(0x00),
    PNG(0x10),
    WSQ(0x20);

    fun getBytes() = byteArrayOf(byte)

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] }
            ?: throw InvalidMessageException("Invalid ImageFormat received with bytes: ${bytes.toHexString()}")
    }
}

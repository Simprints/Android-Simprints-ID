package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException

enum class ImageFormat(val byte: Byte) {
    RAW(0x00),
    PNG(0x10),
    WSQ(0x20);

    fun getBytes() = byteArrayOf(byte)

    companion object {
        fun fromByte(byte: Byte) = values().find { it.byte == byte }
            ?: throw InvalidMessageException("Invalid ImageFormat received with byte: $byte")
    }
}

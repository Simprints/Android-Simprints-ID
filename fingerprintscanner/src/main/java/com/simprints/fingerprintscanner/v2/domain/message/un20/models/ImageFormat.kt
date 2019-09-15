package com.simprints.fingerprintscanner.v2.domain.message.un20.models

enum class ImageFormat(val byte: Byte) {
    RAW(0x00),
    PNG(0x10),
    WSQ(0x20);

    fun getBytes() = byteArrayOf(byte)

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] } ?: TODO()
    }
}

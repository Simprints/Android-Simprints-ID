package com.simprints.fingerprintscanner.v2.domain.message.un20.models

enum class TemplateType(val byte: Byte) {
    ISO_19794_2_2011(0x10);

    fun getBytes() = byteArrayOf(byte)

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] } ?: TODO()
    }
}

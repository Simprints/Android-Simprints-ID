package com.simprints.fingerprintscanner.v2.domain.message.vero.models

import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt

sealed class LedMode(val byte: Byte) {
    object OFF : LedMode(0x00)
    class FLASHING(periodMs: Int): LedMode(calculateByteValueFromPeriod(periodMs))
    object ON : LedMode(0xFF.toByte())

    companion object {
        fun fromBytes(bytes: ByteArray) = when (val value = bytes[0].unsignedToInt()) {
            0 -> OFF
            in 1..254 -> FLASHING(value)
            255 -> ON
            else -> TODO("exception handling")
        }
    }
}

private fun calculateByteValueFromPeriod(periodMs: Int) = (16 * periodMs).toByte()

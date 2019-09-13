package com.simprints.fingerprintscanner.v2.domain.message.vero.models

import com.simprints.fingerprintscanner.v2.tools.unsignedToInt

sealed class LedMode(val byte: Byte) {
    object OFF : LedMode(0x00)
    class FLASHING(periodMs: Int): LedMode(calculateByteValueFromPeriod(periodMs))
    object ON : LedMode(0xFF.toByte())

    companion object {
        fun fromByte(byte: Byte) = when (val value = byte.unsignedToInt()) {
            0 -> OFF
            in 1..254 -> FLASHING(value)
            255 -> ON
            else -> TODO()
        }
    }
}

private fun calculateByteValueFromPeriod(periodMs: Int) = (16 * periodMs).toByte()

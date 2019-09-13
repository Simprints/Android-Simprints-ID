package com.simprints.fingerprintscanner.v2.domain.message.vero.models

sealed class LedMode(val byte: Byte) {
    object OFF : LedMode(0x00)
    class FLASHING(periodMs: Int): LedMode(calculateByteValueFromPeriod(periodMs))
    object ON : LedMode(0xFF.toByte())
}

private fun calculateByteValueFromPeriod(periodMs: Int) = (16 * periodMs).toByte()

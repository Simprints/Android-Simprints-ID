package com.simprints.fingerprintscanner.v2.domain.message.vero.models

data class LedState(val mode: LedMode, val red: Byte, val blue: Byte, val green: Byte) {

    fun getBytes() = byteArrayOf(mode.byte, red, blue, green)
}

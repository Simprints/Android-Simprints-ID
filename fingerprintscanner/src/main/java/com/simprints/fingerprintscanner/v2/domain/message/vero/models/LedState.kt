package com.simprints.fingerprintscanner.v2.domain.message.vero.models

data class LedState(val mode: LedMode, val red: Byte, val blue: Byte, val green: Byte) {

    fun getBytes() = byteArrayOf(mode.byte, red, blue, green)

    companion object {
        fun fromBytes(bytes: ByteArray) = LedState(
            mode = LedMode.fromByte(bytes[0]),
            red = bytes[1],
            blue = bytes[2],
            green = bytes[3]
        )
    }
}

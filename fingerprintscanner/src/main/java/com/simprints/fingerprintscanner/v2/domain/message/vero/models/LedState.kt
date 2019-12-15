package com.simprints.fingerprintscanner.v2.domain.message.vero.models

data class LedState(val isFlashing: DigitalValue, val red: Byte, val green: Byte, val blue: Byte) {

    fun getBytes() = byteArrayOf(blue, green, red, isFlashing.byte)

    companion object {
        fun fromBytes(bytes: ByteArray) = LedState(
            isFlashing = DigitalValue.fromBytes(byteArrayOf(bytes[3])),
            red = bytes[2],
            green = bytes[1],
            blue = bytes[0]
        )
    }
}

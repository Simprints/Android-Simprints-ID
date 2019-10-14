package com.simprints.fingerprintscanner.v2.domain.message.vero.models

enum class VeroMessageType(val majorByte: Byte, val minorByte: Byte) {

    // 0x1_ : Versioning
    GET_FIRMWARE_VERSION(0x10, 0x00),

    // 0x2_ : UN20 control
    GET_UN20_ON(0x20, 0x10),
    SET_UN20_ON(0x20, 0x11),
    UN20_STATE_CHANGE(0x2A, 0x00),

    // 0x3_ : Trigger button control
    GET_TRIGGER_BUTTON_ACTIVE(0x30, 0x10),
    SET_TRIGGER_BUTTON_ACTIVE(0x30, 0x20),
    TRIGGER_BUTTON_PRESSED(0x3A, 0x00),

    // 0x4_ : LED Control
    GET_SMILE_LED_STATE(0x40, 0x10),
    GET_BLUETOOTH_LED_STATE(0x40, 0x20),
    GET_POWER_LED_STATE(0x40, 0x30),

    SET_SMILE_LED_STATE(0x41, 0x10),
    SET_BLUETOOTH_LED_STATE(0x41, 0x20),
    SET_POWER_LED_STATE(0x41, 0x30);

    fun getBytes() = byteArrayOf(majorByte, minorByte)

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find {
            it.majorByte == bytes[0] && it.minorByte == bytes[1]
        } ?: TODO("exception handling")
    }
}

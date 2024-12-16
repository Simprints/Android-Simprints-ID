package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models

import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toHexString

enum class VeroMessageType(
    val majorByte: Byte,
    val minorByte: Byte,
) {
    // 0x1_ : Versioning
    GET_STM_EXTENDED_FIRMWARE_VERSION(0x10, 0x01),

    // 0x2_ : UN20 control
    GET_UN20_ON(0x20, 0x10),
    SET_UN20_ON(0x20, 0x20),
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

    // 0x5_ : Gas gauge information
    GET_BATTERY_PERCENT_CHARGE(0x50, 0x10),
    GET_BATTERY_VOLTAGE(0x50, 0x11),
    GET_BATTERY_CURRENT(0x50, 0x12),
    GET_BATTERY_TEMPERATURE(0x50, 0x13),
    ;

    fun getBytes() = byteArrayOf(majorByte, minorByte)

    companion object {
        fun fromBytes(bytes: ByteArray) = values().find {
            it.majorByte == bytes[0] && it.minorByte == bytes[1]
        }
            ?: throw InvalidMessageException("Invalid VeroMessageType received with bytes: ${bytes.toHexString()}")
    }
}

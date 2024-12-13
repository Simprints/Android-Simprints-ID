package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models

class BatteryPercentCharge(
    val percentCharge: Byte,
) {
    fun getBytes() = byteArrayOf(percentCharge)

    companion object {
        fun fromBytes(bytes: ByteArray) = BatteryPercentCharge(bytes[0])
    }
}

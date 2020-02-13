package com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.BatteryVoltage
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetBatteryVoltageResponse(val batteryVoltage: BatteryVoltage) : VeroResponse(VeroMessageType.GET_BATTERY_VOLTAGE) {

    override fun getDataBytes(): ByteArray = batteryVoltage.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetBatteryVoltageResponse(BatteryVoltage.fromBytes(data))
    }
}

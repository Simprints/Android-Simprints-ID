package com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.BatteryTemperature
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetBatteryTemperatureResponse(val batteryTemperature: BatteryTemperature) : VeroResponse(VeroMessageType.GET_BATTERY_TEMPERATURE) {

    override fun getDataBytes(): ByteArray = batteryTemperature.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetBatteryTemperatureResponse(BatteryTemperature.fromBytes(data))
    }
}

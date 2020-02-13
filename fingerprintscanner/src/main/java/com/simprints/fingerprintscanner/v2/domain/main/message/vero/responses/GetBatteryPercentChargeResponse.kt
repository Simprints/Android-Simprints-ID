package com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.BatteryPercentCharge
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetBatteryPercentChargeResponse(val batteryPercentCharge: BatteryPercentCharge) : VeroResponse(VeroMessageType.GET_BATTERY_PERCENT_CHARGE) {

    override fun getDataBytes(): ByteArray = batteryPercentCharge.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetBatteryPercentChargeResponse(BatteryPercentCharge.fromBytes(data))
    }
}

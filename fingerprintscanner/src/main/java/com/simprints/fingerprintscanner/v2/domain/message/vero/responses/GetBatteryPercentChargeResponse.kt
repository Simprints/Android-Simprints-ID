package com.simprints.fingerprintscanner.v2.domain.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.BatteryPercentCharge
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetBatteryPercentChargeResponse(val batteryPercentCharge: BatteryPercentCharge) : VeroResponse(VeroMessageType.GET_BATTERY_PERCENT_CHARGE) {

    companion object {
        fun fromBytes(data: ByteArray) = GetBatteryPercentChargeResponse(BatteryPercentCharge.fromBytes(data))
    }
}

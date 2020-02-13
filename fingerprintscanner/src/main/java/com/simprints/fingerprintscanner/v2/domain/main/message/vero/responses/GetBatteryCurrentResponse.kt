package com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.BatteryCurrent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetBatteryCurrentResponse(val batteryCurrent: BatteryCurrent) : VeroResponse(VeroMessageType.GET_BATTERY_CURRENT) {

    override fun getDataBytes(): ByteArray = batteryCurrent.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetBatteryCurrentResponse(BatteryCurrent.fromBytes(data))
    }
}

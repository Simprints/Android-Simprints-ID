package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetBatteryPercentChargeCommand : VeroCommand(VeroMessageType.GET_BATTERY_PERCENT_CHARGE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetBatteryPercentChargeCommand()
    }
}

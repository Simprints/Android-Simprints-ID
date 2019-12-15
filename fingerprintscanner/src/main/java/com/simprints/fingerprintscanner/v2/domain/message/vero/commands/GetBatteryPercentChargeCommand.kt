package com.simprints.fingerprintscanner.v2.domain.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetBatteryPercentChargeCommand() : VeroCommand(VeroMessageType.GET_BATTERY_PERCENT_CHARGE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetBatteryPercentChargeCommand()
    }
}

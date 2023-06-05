package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetBatteryTemperatureCommand : VeroCommand(VeroMessageType.GET_BATTERY_TEMPERATURE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetBatteryTemperatureCommand()
    }
}

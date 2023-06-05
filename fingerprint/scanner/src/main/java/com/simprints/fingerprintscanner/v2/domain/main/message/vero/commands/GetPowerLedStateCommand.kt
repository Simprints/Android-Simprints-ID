package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetPowerLedStateCommand : VeroCommand(VeroMessageType.GET_POWER_LED_STATE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetPowerLedStateCommand()
    }
}

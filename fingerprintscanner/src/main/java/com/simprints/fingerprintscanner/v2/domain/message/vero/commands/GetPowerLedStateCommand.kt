package com.simprints.fingerprintscanner.v2.domain.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetPowerLedStateCommand : VeroCommand(VeroMessageType.GET_POWER_LED_STATE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetPowerLedStateCommand()
    }
}

package com.simprints.fingerprintscanner.v2.domain.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetPowerLedStateCommand(val smileLedState: SmileLedState) : VeroCommand(VeroMessageType.SET_POWER_LED_STATE) {

    override fun getDataBytes(): ByteArray = smileLedState.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) =
            SetPowerLedStateCommand(SmileLedState.fromBytes(data))
    }
}

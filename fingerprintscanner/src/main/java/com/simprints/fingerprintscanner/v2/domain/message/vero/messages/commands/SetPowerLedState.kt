package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetPowerLedState(val smileLedState: SmileLedState) : VeroCommand(VeroMessageType.SET_POWER_LED_STATE) {

    override fun getDataBytes(): ByteArray = smileLedState.getBytes()
}

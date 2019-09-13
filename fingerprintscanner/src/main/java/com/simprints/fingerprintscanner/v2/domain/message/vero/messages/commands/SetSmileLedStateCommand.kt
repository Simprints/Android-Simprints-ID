package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetSmileLedStateCommand(val ledState: LedState) : VeroCommand(VeroMessageType.SET_SMILE_LED_STATE) {

    override fun getDataBytes(): ByteArray = ledState.getBytes()
}

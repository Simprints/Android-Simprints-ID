package com.simprints.fingerprintscanner.v2.domain.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetSmileLedStateCommand(val smileLedState: SmileLedState) : VeroCommand(VeroMessageType.SET_SMILE_LED_STATE) {

    override fun getDataBytes(): ByteArray = smileLedState.getBytes()
}

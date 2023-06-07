package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class SetSmileLedStateCommand(val smileLedState: SmileLedState) : VeroCommand(VeroMessageType.SET_SMILE_LED_STATE) {

    override fun getDataBytes(): ByteArray = smileLedState.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = SetSmileLedStateCommand(SmileLedState.fromBytes(data))
    }
}

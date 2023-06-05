package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetSmileLedStateCommand : VeroCommand(VeroMessageType.GET_SMILE_LED_STATE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetSmileLedStateCommand()
    }
}

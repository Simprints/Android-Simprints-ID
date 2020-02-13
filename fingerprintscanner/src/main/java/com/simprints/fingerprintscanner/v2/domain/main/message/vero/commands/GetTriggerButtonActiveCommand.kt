package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetTriggerButtonActiveCommand : VeroCommand(VeroMessageType.GET_TRIGGER_BUTTON_ACTIVE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetTriggerButtonActiveCommand()
    }
}

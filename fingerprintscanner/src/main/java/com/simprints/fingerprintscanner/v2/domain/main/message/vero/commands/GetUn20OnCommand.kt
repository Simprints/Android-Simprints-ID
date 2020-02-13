package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetUn20OnCommand : VeroCommand(VeroMessageType.GET_UN20_ON) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetUn20OnCommand()
    }
}

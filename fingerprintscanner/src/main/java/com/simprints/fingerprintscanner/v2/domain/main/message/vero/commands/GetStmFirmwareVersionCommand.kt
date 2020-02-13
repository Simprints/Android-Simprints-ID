package com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetStmFirmwareVersionCommand : VeroCommand(VeroMessageType.GET_STM_FIRMWARE_VERSION) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetStmFirmwareVersionCommand()
    }
}

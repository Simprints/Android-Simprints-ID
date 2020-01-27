package com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class GetUn20AppVersionCommand : Un20Command(Un20MessageType.GetUn20AppVersion) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetUn20AppVersionCommand()
    }
}

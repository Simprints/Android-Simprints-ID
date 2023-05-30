package com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class StartOtaCommand(val fileName: String) : Un20Command(Un20MessageType.StartOta) {

    override fun getDataBytes(): ByteArray = fileName.toByteArray(Charsets.UTF_8)

    companion object {
        fun fromBytes(data: ByteArray) = StartOtaCommand(data.toString(Charsets.UTF_8))
    }
}

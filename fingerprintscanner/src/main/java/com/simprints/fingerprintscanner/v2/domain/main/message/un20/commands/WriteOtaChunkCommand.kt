package com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class WriteOtaChunkCommand(val firmwareChunk: ByteArray) : Un20Command(Un20MessageType.StartOta) {

    override fun getDataBytes(): ByteArray = firmwareChunk

    companion object {
        fun fromBytes(data: ByteArray) = WriteOtaChunkCommand(data)
    }
}

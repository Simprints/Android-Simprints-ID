package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class WriteOtaChunkCommand(
    val firmwareChunk: ByteArray,
) : Un20Command(Un20MessageType.WriteOtaChunk) {
    override fun getDataBytes(): ByteArray = firmwareChunk

    companion object {
        fun fromBytes(data: ByteArray) = WriteOtaChunkCommand(data)
    }
}

package com.simprints.fingerprintscanner.v2.domain.stmota.responses

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString

class CommandAcknowledgement(val kind: Kind) : StmOtaResponse() {

    override fun getDataBytes(): ByteArray = byteArrayOf(kind.byte)

    enum class Kind(val byte: Byte) {
        ACK(0x79.toByte()),
        NACK(0x1F.toByte())
    }

    companion object {
        fun fromBytes(bytes: ByteArray) = CommandAcknowledgement(Kind.values().find { it.byte == bytes[0] }
            ?: throw TODO("Unexpected STM OTA command acknowledgement value : ${bytes.toHexString()}"))
    }
}

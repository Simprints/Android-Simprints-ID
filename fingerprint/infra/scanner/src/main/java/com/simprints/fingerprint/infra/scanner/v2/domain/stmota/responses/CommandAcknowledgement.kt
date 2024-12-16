package com.simprints.fingerprint.infra.scanner.v2.domain.stmota.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toHexString

class CommandAcknowledgement(
    val kind: Kind,
) : StmOtaResponse() {
    override fun getDataBytes(): ByteArray = byteArrayOf(kind.byte)

    enum class Kind(
        val byte: Byte,
    ) {
        ACK(0x79.toByte()),
        NACK(0x1F.toByte()),
    }

    companion object {
        fun fromBytes(bytes: ByteArray) = CommandAcknowledgement(
            Kind.values().find { it.byte == bytes[0] }
                ?: throw InvalidMessageException("Invalid CommandAcknowledgement received with bytes: ${bytes.toHexString()}"),
        )
    }
}

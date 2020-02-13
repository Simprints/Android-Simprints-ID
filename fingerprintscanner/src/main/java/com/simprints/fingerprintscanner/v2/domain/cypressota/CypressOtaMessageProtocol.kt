package com.simprints.fingerprintscanner.v2.domain.cypressota

import com.simprints.fingerprintscanner.v2.domain.Protocol
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import java.nio.ByteOrder

object CypressOtaMessageProtocol : Protocol {
    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN

    const val MAX_PAYLOAD_SIZE = 253

    const val HEADER_SIZE: Int = 3
    const val MESSAGE_TYPE_INDEX_IN_HEADER: Int = 0

    fun getMessageType(messageBytes: ByteArray): CypressOtaResponseType =
        CypressOtaResponseType.fromByte(messageBytes[MESSAGE_TYPE_INDEX_IN_HEADER])

    fun buildMessageBytes(cypressOtaCommandType: CypressOtaCommandType, data: ByteArray): ByteArray {
        val length = data.size
        val header = byteArrayOf(cypressOtaCommandType.byte, length.toShort().toByteArray())
        return header + data
    }
}

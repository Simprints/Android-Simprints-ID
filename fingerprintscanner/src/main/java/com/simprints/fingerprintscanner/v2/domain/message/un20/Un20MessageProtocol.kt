package com.simprints.fingerprintscanner.v2.domain.message.un20

import com.simprints.fingerprintscanner.v2.domain.message.MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType
import java.nio.ByteOrder

object Un20MessageProtocol: MessageProtocol {

    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val HEADER_SIZE: Int = 6
    override val HEADER_INDICES: IntRange = 0..5
    override val MESSAGE_TYPE_INDICES_IN_HEADER: IntRange = 0..1
    override val LENGTH_INDICES_IN_HEADER: IntRange = 2..5

    private const val MINOR_MESSAGE_TYPE_INDEX = 1

    fun getMinorTypeByte(messageBytes: ByteArray): Byte =
        messageBytes[MINOR_MESSAGE_TYPE_INDEX]

    override fun getDataLengthFromHeader(header: ByteArray): Int =
        header.extract({ int },
            LENGTH_INDICES_IN_HEADER
        )

    fun getMessageType(messageBytes: ByteArray) =
        Un20MessageType.fromBytes(messageBytes.sliceArray(MESSAGE_TYPE_INDICES_IN_HEADER))

    fun buildMessageBytes(un20MessageType: Un20MessageType, data: ByteArray): ByteArray {
        val length = data.size
        val header = un20MessageType.getBytes() + length.toByteArray()
        return header + data
    }
}

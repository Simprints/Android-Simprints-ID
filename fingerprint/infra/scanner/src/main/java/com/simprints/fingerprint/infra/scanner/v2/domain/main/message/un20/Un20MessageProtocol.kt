package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType
import java.nio.ByteOrder

object Un20MessageProtocol : MessageProtocol {
    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val headerSize: Int = 6
    override val headerIndices: IntRange = 0..5
    override val messageTypeIndicesInHeader: IntRange = 0..1
    override val lengthIndicesInHeader: IntRange = 2..5

    private const val MINOR_MESSAGE_TYPE_INDEX = 1

    fun getMinorTypeByte(messageBytes: ByteArray): Byte = messageBytes[MINOR_MESSAGE_TYPE_INDEX]

    override fun getDataLengthFromHeader(header: ByteArray): Int = header.extract(
        { int },
        lengthIndicesInHeader,
    )

    fun getMessageType(messageBytes: ByteArray) = Un20MessageType.fromBytes(messageBytes.sliceArray(messageTypeIndicesInHeader))

    fun buildMessageBytes(
        un20MessageType: Un20MessageType,
        data: ByteArray,
    ): ByteArray {
        val length = data.size
        val header = un20MessageType.getBytes() + length.toByteArray()
        return header + data
    }
}

package com.simprints.fingerprintscanner.v2.domain.root

import com.simprints.fingerprintscanner.v2.domain.Protocol
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt
import java.nio.ByteOrder

object RootMessageProtocol : Protocol {

    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN

    const val HEADER_SIZE: Int = 4
    val HEADER_INDICES: IntRange = 0..3
    val LENGTH_INDICES_IN_HEADER: IntRange = 2..3
    val MESSAGE_TYPE_INDEX_IN_HEADER: Int = 1

    const val START_BYTE = 0xF4.toByte()

    fun getDataLengthFromHeader(header: ByteArray): Int =
        header.extract({ short },
            LENGTH_INDICES_IN_HEADER
        ).unsignedToInt()

    fun getTotalLengthFromHeader(header: ByteArray): Int =
        getDataLengthFromHeader(header) + HEADER_SIZE

    fun getDataBytes(messageBytes: ByteArray): ByteArray =
        messageBytes.sliceArray(HEADER_SIZE until messageBytes.size)

    fun getMessageType(messageBytes: ByteArray): RootMessageType =
        RootMessageType.fromByte(messageBytes[MESSAGE_TYPE_INDEX_IN_HEADER])

    fun buildMessageBytes(rootMessageType: RootMessageType, data: ByteArray): ByteArray {
        val length = data.size
        val header = byteArrayOf(START_BYTE, rootMessageType.byte, length.toShort().toByteArray())
        return header + data
    }
}

package com.simprints.fingerprintscanner.v2.domain.message.vero

import com.simprints.fingerprintscanner.v2.domain.message.MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType
import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt
import java.nio.ByteOrder

object VeroMessageProtocol: MessageProtocol {

    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val HEADER_SIZE: Int = 4
    override val HEADER_INDICES: IntRange = 0..3
    override val MESSAGE_TYPE_INDICES_IN_HEADER: IntRange = 0..1
    override val LENGTH_INDICES_IN_HEADER: IntRange = 2..3

    override fun getDataLengthFromHeader(header: ByteArray): Int =
        header.extract({ short },
            LENGTH_INDICES_IN_HEADER
        ).unsignedToInt()

    fun getMessageType(messageBytes: ByteArray): VeroMessageType =
        VeroMessageType.fromBytes(messageBytes.sliceArray(MESSAGE_TYPE_INDICES_IN_HEADER))

    fun buildMessageBytes(veroMessageType: VeroMessageType, data: ByteArray): ByteArray {
        val length = data.size
        val header = veroMessageType.getBytes() + length.toShort().toByteArray()
        return header + data
    }
}

package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.unsignedToInt
import java.nio.ByteOrder

object VeroMessageProtocol : MessageProtocol {
    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val headerSize: Int = 4
    override val headerIndices: IntRange = 0..3
    override val messageTypeIndicesInHeader: IntRange = 0..1
    override val lengthIndicesInHeader: IntRange = 2..3

    override fun getDataLengthFromHeader(header: ByteArray): Int = header
        .extract(
            { short },
            lengthIndicesInHeader,
        ).unsignedToInt()

    fun getMessageType(messageBytes: ByteArray): VeroMessageType =
        VeroMessageType.fromBytes(messageBytes.sliceArray(messageTypeIndicesInHeader))

    fun buildMessageBytes(
        veroMessageType: VeroMessageType,
        data: ByteArray,
    ): ByteArray {
        val length = data.size
        val header = veroMessageType.getBytes() + length.toShort().toByteArray()
        return header + data
    }
}

package com.simprints.fingerprintscanner.v2.message.un20

import com.simprints.fingerprintscanner.v2.message.MessageProtocol
import java.nio.ByteOrder

object Un20MessageProtocol: MessageProtocol {

    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val HEADER_SIZE: Int = 6
    override val HEADER_INDICES: IntRange = 0..5
    override val LENGTH_INDICES_IN_HEADER: IntRange = 2..5

    override fun getDataLengthFromHeader(header: ByteArray): Int =
        header.extract({ int },
            LENGTH_INDICES_IN_HEADER
        )
}

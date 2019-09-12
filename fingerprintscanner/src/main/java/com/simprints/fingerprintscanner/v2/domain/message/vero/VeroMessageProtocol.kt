package com.simprints.fingerprintscanner.v2.domain.message.vero

import com.simprints.fingerprintscanner.v2.domain.message.MessageProtocol
import com.simprints.fingerprintscanner.v2.tools.unsignedToInt
import java.nio.ByteOrder

object VeroMessageProtocol: MessageProtocol {

    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
    override val HEADER_SIZE: Int = 4
    override val HEADER_INDICES: IntRange = 0..3
    override val LENGTH_INDICES_IN_HEADER: IntRange = 2..3

    override fun getDataLengthFromHeader(header: ByteArray): Int =
        header.extract({ short },
            LENGTH_INDICES_IN_HEADER
        ).unsignedToInt()
}

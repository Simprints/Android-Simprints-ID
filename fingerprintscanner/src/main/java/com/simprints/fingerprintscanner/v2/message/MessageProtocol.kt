package com.simprints.fingerprintscanner.v2.message

import com.simprints.fingerprintscanner.v2.tools.Protocol
import com.simprints.fingerprintscanner.v2.tools.extract
import java.nio.ByteBuffer

interface MessageProtocol: Protocol {

    val HEADER_INDICES: IntRange
    val LENGTH_INDICES_IN_HEADER: IntRange

    val HEADER_SIZE: Int

    fun getMessageLengthFromHeader(header: ByteArray): Int

    fun <T> ByteArray.extract(getType: ByteBuffer.() -> T, position: IntRange? = null): T =
        extract(getType, position, byteOrder)
}

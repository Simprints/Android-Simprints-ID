package com.simprints.fingerprintscanner.v2.message

import com.simprints.fingerprintscanner.v2.tools.Protocol

interface MessageProtocol: Protocol {

    val HEADER_INDICES: IntRange
    val LENGTH_INDICES_IN_HEADER: IntRange

    val HEADER_SIZE: Int

    fun getDataLengthFromHeader(header: ByteArray): Int

    fun getTotalLengthFromHeader(header: ByteArray): Int =
        getDataLengthFromHeader(header) + HEADER_SIZE
}

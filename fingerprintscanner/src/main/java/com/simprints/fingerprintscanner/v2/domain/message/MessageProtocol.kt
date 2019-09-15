package com.simprints.fingerprintscanner.v2.domain.message

import com.simprints.fingerprintscanner.v2.domain.Protocol

interface MessageProtocol: Protocol {

    val HEADER_SIZE: Int
    val HEADER_INDICES: IntRange
    val LENGTH_INDICES_IN_HEADER: IntRange
    val MESSAGE_TYPE_INDICES_IN_HEADER: IntRange

    fun getDataLengthFromHeader(header: ByteArray): Int

    fun getTotalLengthFromHeader(header: ByteArray): Int =
        getDataLengthFromHeader(header) + HEADER_SIZE

    fun getDataBytes(messageBytes: ByteArray): ByteArray =
        messageBytes.sliceArray(HEADER_SIZE..messageBytes.size)
}

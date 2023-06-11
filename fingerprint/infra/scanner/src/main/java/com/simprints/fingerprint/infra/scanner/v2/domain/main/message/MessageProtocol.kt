package com.simprints.fingerprint.infra.scanner.v2.domain.main.message

import com.simprints.fingerprint.infra.scanner.v2.domain.Protocol

interface MessageProtocol: Protocol {

    val HEADER_SIZE: Int
    val HEADER_INDICES: IntRange
    val LENGTH_INDICES_IN_HEADER: IntRange
    val MESSAGE_TYPE_INDICES_IN_HEADER: IntRange

    fun getDataLengthFromHeader(header: ByteArray): Int

    fun getTotalLengthFromHeader(header: ByteArray): Int =
        getDataLengthFromHeader(header) + HEADER_SIZE

    fun getDataBytes(messageBytes: ByteArray): ByteArray =
        messageBytes.sliceArray(HEADER_SIZE until messageBytes.size)
}

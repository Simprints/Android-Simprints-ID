package com.simprints.fingerprint.infra.scanner.v2.domain.main.message

import com.simprints.fingerprint.infra.scanner.v2.domain.Protocol

interface MessageProtocol : Protocol {
    val headerSize: Int
    val headerIndices: IntRange
    val lengthIndicesInHeader: IntRange
    val messageTypeIndicesInHeader: IntRange

    fun getDataLengthFromHeader(header: ByteArray): Int

    fun getTotalLengthFromHeader(header: ByteArray): Int = getDataLengthFromHeader(header) + headerSize

    fun getDataBytes(messageBytes: ByteArray): ByteArray = messageBytes.sliceArray(headerSize until messageBytes.size)
}

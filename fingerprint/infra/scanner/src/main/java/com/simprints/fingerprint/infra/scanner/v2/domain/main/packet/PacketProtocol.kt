package com.simprints.fingerprint.infra.scanner.v2.domain.main.packet

import com.simprints.fingerprint.infra.scanner.v2.domain.Protocol
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.unsignedToInt
import java.nio.ByteOrder

object PacketProtocol : Protocol {
    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN

    const val MAX_PAYLOAD_SIZE = 900

    val HEADER_INDICES = 0..3
    private val SOURCE_INDICES_IN_HEADER = 0..0
    private val DESTINATION_INDICES_IN_HEADER = 1..1
    private val LENGTH_INDICES_IN_HEADER = 2..3

    val HEADER_SIZE = HEADER_INDICES.count()

    fun getSourceFromHeader(header: ByteArray): Byte = header.extract({ get() }, SOURCE_INDICES_IN_HEADER)

    fun getDestinationFromHeader(header: ByteArray): Byte = header.extract({ get() }, DESTINATION_INDICES_IN_HEADER)

    fun getTotalLengthFromHeader(header: ByteArray): Int = getPayloadLengthFromHeader(header) + HEADER_SIZE

    fun getPayloadLengthFromHeader(header: ByteArray): Int = header.extract({ short }, LENGTH_INDICES_IN_HEADER).unsignedToInt()

    fun getHeaderBytes(bytes: ByteArray): ByteArray = bytes.slice(HEADER_INDICES).toByteArray()

    fun getPayloadBytes(bytes: ByteArray): ByteArray = bytes.slice(HEADER_SIZE until bytes.size).toByteArray()

    fun buildPacketBytes(
        source: Route,
        destination: Route,
        payload: ByteArray,
    ): ByteArray {
        val length = payload.size
        val header = byteArrayOf(source.id.value, destination.id.value) + length.toShort().toByteArray()
        return header + payload
    }
}

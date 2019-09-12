package com.simprints.fingerprintscanner.v2.packets

import com.simprints.fingerprintscanner.v2.tools.*
import java.nio.ByteOrder

object PacketProtocol: Protocol {

    override val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN

    val HEADER_INDICES = 0..3
    private val SOURCE_INDICES_IN_HEADER = 0..0
    private val DESTINATION_INDICES_IN_HEADER = 1..1
    private val LENGTH_INDICES_IN_HEADER = 2..3

    val HEADER_SIZE = HEADER_INDICES.size()

    fun getSourceFromHeader(header: ByteArray): Int =
        header.extract({ get() }, SOURCE_INDICES_IN_HEADER).unsignedToInt()

    fun getDestinationFromHeader(header: ByteArray): Int =
        header.extract({ get() }, DESTINATION_INDICES_IN_HEADER).unsignedToInt()

    fun getTotalLengthFromHeader(header: ByteArray): Int =
        getPayloadLengthFromHeader(header) + HEADER_SIZE

    fun getPayloadLengthFromHeader(header: ByteArray): Int =
        header.extract({ short }, LENGTH_INDICES_IN_HEADER).unsignedToInt()

    fun getHeaderBytes(bytes: ByteArray): ByteArray =
        bytes.slice(HEADER_INDICES).toByteArray()

    fun getPayloadBytes(bytes: ByteArray): ByteArray =
        bytes.slice(HEADER_INDICES.last + 1 until bytes.size).toByteArray()

    fun buildPacketBytes(source: Channel, destination: Channel, payload: ByteArray): ByteArray {
        val length = payload.size
        val header = byteArrayOf(source.id.toByte(), destination.id.toByte()) + length.toShort().toByteArray()
        return header + payload
    }
}

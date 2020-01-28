package com.simprints.fingerprintscanner.v2.incoming.main.packet

import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.domain.main.packet.PacketProtocol
import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidPacketException
import java.nio.BufferUnderflowException

class PacketParser {

    /** @throws InvalidPacketException */
    fun parse(bytes: ByteArray): Packet =
        try {
            PacketProtocol.getHeaderBytes(bytes).let { header ->
                Packet(
                    bytes = bytes,
                    header = header,
                    payload = PacketProtocol.getPayloadBytes(bytes),
                    source = PacketProtocol.getSourceFromHeader(header),
                    destination = PacketProtocol.getDestinationFromHeader(header),
                    payloadLength = PacketProtocol.getPayloadLengthFromHeader(header)
                )
            }
        } catch (e: Exception) {
            handleExceptionDuringParsing(e)
        }

    private fun handleExceptionDuringParsing(e: Throwable): Nothing =
        when (e) {
            is InvalidPacketException ->
                throw e
            is IndexOutOfBoundsException, is BufferUnderflowException ->
                throw InvalidPacketException("Incorrect number of bytes received parsing response in ${this::class.java.simpleName}", e)
            else ->
                throw InvalidPacketException("Unknown issue during parsing in ${this::class.java.simpleName}", e)
        }
}

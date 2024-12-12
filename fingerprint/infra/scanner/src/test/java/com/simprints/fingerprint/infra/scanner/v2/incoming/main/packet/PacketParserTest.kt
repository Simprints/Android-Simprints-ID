package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.simprints.fingerprint.infra.scanner.testtools.assertPacketsEqual
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidPacketException
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class PacketParserTest {
    @Test
    fun parsePacket_buildsPacketCorrectlyFromProtocol() {
        val packetParser = PacketParser()

        val rawBytes = "10 A0 08 00 F0 F1 F2 F3".hexToByteArray()
        val expectedPacket = Packet(
            bytes = "10 A0 08 00 F0 F1 F2 F3".hexToByteArray(),
            header = "10 A0 08 00".hexToByteArray(),
            payload = "F0 F1 F2 F3".hexToByteArray(),
            source = 0x10,
            destination = 0xA0.toByte(),
            payloadLength = 8,
        )
        val actualPacket = packetParser.parse(rawBytes)

        assertPacketsEqual(expectedPacket, actualPacket)
    }

    @Test
    fun parsePacket_givenPacketTooShortForHeader_throwsException() {
        val packetParser = PacketParser()

        val rawBytes = "10 A0 08".hexToByteArray()

        assertThrows<InvalidPacketException> { packetParser.parse(rawBytes) }
    }
}

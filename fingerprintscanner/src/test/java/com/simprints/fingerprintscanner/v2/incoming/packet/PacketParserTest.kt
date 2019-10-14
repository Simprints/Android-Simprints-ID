package com.simprints.fingerprintscanner.v2.incoming.packet

import com.simprints.fingerprintscanner.testtools.assertPacketsEqual
import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
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
            payloadLength = 8
        )
        val actualPacket = packetParser.parse(rawBytes)

        assertPacketsEqual(expectedPacket, actualPacket)
    }
}

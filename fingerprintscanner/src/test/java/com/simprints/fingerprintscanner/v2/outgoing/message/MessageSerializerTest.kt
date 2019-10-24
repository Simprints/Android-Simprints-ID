package com.simprints.fingerprintscanner.v2.outgoing.message

import com.simprints.fingerprintscanner.testtools.assertPacketsEqual
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.incoming.packet.PacketParser
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import org.junit.Test

class MessageSerializerTest {

    @Test
    fun veroCommand_serialized_producesCorrectPackets() {
        val packetParser = PacketParser()

        val message = SetUn20OnCommand(DigitalValue.TRUE)
        val expectedPackets = listOf(packetParser.parse("A0 10 05 00 20 11 01 00 FF".hexToByteArray()))

        val messageSerializer = MessageSerializer(packetParser)

        assertPacketsEqual(expectedPackets, messageSerializer.serialize(message))
    }

    @Test
    fun un20Command_serialized_producesCorrectPackets() {
        val packetParser = PacketParser()

        val message = GetTemplateCommand(TemplateType.ISO_19794_2_2011)
        val expectedPackets = listOf(packetParser.parse("A0 20 06 00 31 10 00 00 00 00".hexToByteArray()))

        val messageSerializer = MessageSerializer(packetParser)

        assertPacketsEqual(expectedPackets, messageSerializer.serialize(message))
    }
}

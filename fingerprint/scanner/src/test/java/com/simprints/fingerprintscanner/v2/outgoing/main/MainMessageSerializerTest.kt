package com.simprints.fingerprintscanner.v2.outgoing.main

import com.simprints.fingerprintscanner.testtools.assertHexStringsEqual
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString
import org.junit.Test

class MainMessageSerializerTest {

    @Test
    fun veroCommand_serialized_producesCorrectPackets() {
        val message = SetUn20OnCommand(DigitalValue.TRUE)
        val expectedBytes = listOf("A0 10 05 00 20 20 01 00 FF")

        val messageSerializer = MainMessageSerializer()

        assertHexStringsEqual(messageSerializer.serialize(message).map { it.toHexString() }, expectedBytes)
    }

    @Test
    fun un20Command_serialized_producesCorrectPackets() {
        val message = GetTemplateCommand(TemplateType.ISO_19794_2_2011)
        val expectedBytes = listOf("A0 20 06 00 31 10 00 00 00 00")

        val messageSerializer = MainMessageSerializer()

        assertHexStringsEqual(messageSerializer.serialize(message).map { it.toHexString() }, expectedBytes)
    }
}

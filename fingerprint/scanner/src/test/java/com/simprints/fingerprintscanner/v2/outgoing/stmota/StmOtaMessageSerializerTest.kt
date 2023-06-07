package com.simprints.fingerprintscanner.v2.outgoing.stmota

import com.simprints.fingerprintscanner.testtools.assertHexStringsEqual
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString
import org.junit.Test

class StmOtaMessageSerializerTest {

    @Test
    fun stmOtaCommand_serialized_producesCorrectPackets() {
        val message = WriteMemoryStartCommand()
        val expectedBytes = listOf("31 CE")

        val stmOtaMessageSerializer = StmOtaMessageSerializer()
        val actualBytes = stmOtaMessageSerializer.serialize(message).map { it.toHexString() }

        assertHexStringsEqual(expectedBytes, actualBytes)
    }
}

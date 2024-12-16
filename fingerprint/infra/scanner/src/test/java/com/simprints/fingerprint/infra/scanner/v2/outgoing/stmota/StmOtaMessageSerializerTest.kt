package com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota

import com.simprints.fingerprint.infra.scanner.testtools.assertHexStringsEqual
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toHexString
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

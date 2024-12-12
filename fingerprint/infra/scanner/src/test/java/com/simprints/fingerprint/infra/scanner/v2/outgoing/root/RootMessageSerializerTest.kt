package com.simprints.fingerprint.infra.scanner.v2.outgoing.root

import com.simprints.fingerprint.infra.scanner.testtools.assertHexStringsEqual
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toHexString
import org.junit.Test

class RootMessageSerializerTest {
    @Test
    fun rootCommand_serialized_producesCorrectPackets() {
        val message = EnterMainModeCommand()
        val expectedBytes = listOf("F4 10 00 00")

        val rootMessageSerializer = RootMessageSerializer()
        val actualBytes = rootMessageSerializer.serialize(message).map { it.toHexString() }

        assertHexStringsEqual(expectedBytes, actualBytes)
    }
}

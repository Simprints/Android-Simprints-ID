package com.simprints.fingerprintscanner.v2.outgoing.cypressota

import com.simprints.fingerprintscanner.testtools.assertHexStringsEqual
import com.simprints.fingerprintscanner.v2.domain.cypressota.commands.DownloadCommand
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString
import org.junit.Test

class CypressOtaMessageSerializerTest {

    @Test
    fun cypressOtaCommand_serialized_producesCorrectPackets() {
        val message = DownloadCommand(5000)
        val expectedBytes = listOf("12 04 00 88 13 00 00")

        val cypressOtaMessageSerializer = CypressOtaMessageSerializer()
        val actualBytes = cypressOtaMessageSerializer.serialize(message).map { it.toHexString() }

        assertHexStringsEqual(expectedBytes, actualBytes)
    }
}

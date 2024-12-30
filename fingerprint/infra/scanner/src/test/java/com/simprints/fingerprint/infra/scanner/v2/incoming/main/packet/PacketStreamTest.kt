package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.testtools.hollowPacketWithRawBytes
import com.simprints.fingerprint.infra.scanner.testtools.reduceString
import com.simprints.fingerprint.infra.scanner.testtools.stripWhiteSpaceToLowercase
import com.simprints.fingerprint.infra.scanner.testtools.toHexStrings
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PacketStreamTest {
    private val packetParserMock = mockk<PacketParser> {
        every { parse(any()) } answers {
            hollowPacketWithRawBytes(args[0] as ByteArray)
        }
    }

    @Test
    fun packetStream_accumulatesAndCreatesPacketsCorrectly() = runTest {
        val packetStrings = listOf("10 A0 01 00 F0", "10 A0 03 00 F0 F1 F2", "10 A0 02 00 F0 F1")

        val testFlow = packetStrings
            .reduceString()
            .hexToByteArray()
            .chunked(4)
            .asFlow()
            .toPacketStream(ByteArrayToPacketAccumulator(packetParserMock))

        assertThat(
            testFlow
                .toList()
                .map { it.bytes }
                .toHexStrings()
                .stripWhiteSpaceToLowercase(),
        ).containsExactlyElementsIn(packetStrings.stripWhiteSpaceToLowercase())
            .inOrder()
    }
}

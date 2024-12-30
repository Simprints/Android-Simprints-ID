package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.testtools.TestMessage
import com.simprints.fingerprint.infra.scanner.testtools.TestMessageAccumulator
import com.simprints.fingerprint.infra.scanner.testtools.TestMessageParser
import com.simprints.fingerprint.infra.scanner.testtools.hollowPacketWithPayload
import com.simprints.fingerprint.infra.scanner.testtools.lowerHexStrings
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MessageChannelTest {
    @Test
    fun packetToMessageAccumulation_messageSpreadOutOverMultiplePackets_succeeds() = runTest {
        val packets = listOf("CC DD 08 00", "F0 F1 F2 F3 F4", "F5 F6 F7")
            .map { hollowPacketWithPayload(it.hexToByteArray()) }
        val messages = listOf("CC DD 08 00 F0 F1 F2 F3 F4 F5 F6 F7 ")
            .map { TestMessage(it.hexToByteArray()) }

        val testSubscriber = packets
            .asFlow()
            .toMainMessageStream(TestMessageAccumulator(TestMessageParser()))

        assertThat(testSubscriber.toList().lowerHexStrings())
            .containsExactlyElementsIn(messages.lowerHexStrings())
            .inOrder()
    }

    @Test
    fun packetToMessageAccumulation_packetContainsMultipleMessages_succeeds() = runTest {
        val packets = listOf("CC DD 02 00 F0 F2 CC DD 00 00 CC DD 01 00 F0", "CC DD 00 00 CC DD 03 00 F0 F1 F2")
            .map { hollowPacketWithPayload(it.hexToByteArray()) }
        val messages = listOf("CC DD 02 00 F0 F2", "CC DD 00 00", "CC DD 01 00 F0", "CC DD 00 00", "CC DD 03 00 F0 F1 F2")
            .map { TestMessage(it.hexToByteArray()) }

        val testSubscriber = packets
            .asFlow()
            .toMainMessageStream(TestMessageAccumulator(TestMessageParser()))

        assertThat(testSubscriber.toList().lowerHexStrings())
            .containsExactlyElementsIn(messages.lowerHexStrings())
            .inOrder()
    }

    @Test
    fun packetToMessageAccumulation_multipleMessagesBrokenOverMultiplePackets_succeeds() = runTest {
        val packets = listOf("CC DD 01 00", "F0 CC DD 03", "00 F0 F1 F2")
            .map { hollowPacketWithPayload(it.hexToByteArray()) }
        val messages = listOf("CC DD 01 00 F0", "CC DD 03 00 F0 F1 F2")
            .map { TestMessage(it.hexToByteArray()) }

        val testSubscriber = packets
            .asFlow()
            .toMainMessageStream(TestMessageAccumulator(TestMessageParser()))

        assertThat(testSubscriber.toList().lowerHexStrings())
            .containsExactlyElementsIn(messages.lowerHexStrings())
            .inOrder()
    }
}

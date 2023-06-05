package com.simprints.fingerprintscanner.v2.incoming.main.message

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.*
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.rxkotlin.toFlowable
import org.junit.Test

class MessageChannelTest {

    @Test
    fun packetToMessageAccumulation_messageSpreadOutOverMultiplePackets_succeeds() {
        val packets = listOf("CC DD 08 00", "F0 F1 F2 F3 F4", "F5 F6 F7")
            .map { hollowPacketWithPayload(it.hexToByteArray()) }
        val messages = listOf("CC DD 08 00 F0 F1 F2 F3 F4 F5 F6 F7 ")
            .map { TestMessage(it.hexToByteArray()) }

        val testSubscriber = packets
            .toFlowable()
            .toMainMessageStream(TestMessageAccumulator(TestMessageParser()))
            .testSubscribe()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().lowerHexStrings())
            .containsExactlyElementsIn(messages.lowerHexStrings())
            .inOrder()
    }

    @Test
    fun packetToMessageAccumulation_packetContainsMultipleMessages_succeeds() {
        val packets = listOf("CC DD 02 00 F0 F2 CC DD 00 00 CC DD 01 00 F0", "CC DD 00 00 CC DD 03 00 F0 F1 F2")
            .map { hollowPacketWithPayload(it.hexToByteArray()) }
        val messages = listOf("CC DD 02 00 F0 F2", "CC DD 00 00", "CC DD 01 00 F0", "CC DD 00 00", "CC DD 03 00 F0 F1 F2")
            .map { TestMessage(it.hexToByteArray()) }

        val testSubscriber = packets
            .toFlowable()
            .toMainMessageStream(TestMessageAccumulator(TestMessageParser()))
            .testSubscribe()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().lowerHexStrings())
            .containsExactlyElementsIn(messages.lowerHexStrings())
            .inOrder()
    }

    @Test
    fun packetToMessageAccumulation_multipleMessagesBrokenOverMultiplePackets_succeeds() {
        val packets = listOf("CC DD 01 00", "F0 CC DD 03", "00 F0 F1 F2")
            .map { hollowPacketWithPayload(it.hexToByteArray()) }
        val messages = listOf("CC DD 01 00 F0", "CC DD 03 00 F0 F1 F2")
            .map { TestMessage(it.hexToByteArray()) }

        val testSubscriber = packets
            .toFlowable()
            .toMainMessageStream(TestMessageAccumulator(TestMessageParser()))
            .testSubscribe()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().lowerHexStrings())
            .containsExactlyElementsIn(messages.lowerHexStrings())
            .inOrder()
    }
}

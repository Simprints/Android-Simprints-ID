package com.simprints.fingerprintscanner.v2.incoming.main.packet

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.*
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.setupMock
import com.simprints.testtools.common.syntax.whenThis
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.rxkotlin.toFlowable
import org.junit.Test

class PacketStreamTest {

    private val packetParserMock = setupMock<PacketParser> {
        whenThis { parse(anyNotNull()) } then {
            hollowPacketWithRawBytes(it.arguments[0] as ByteArray)
        }
    }

    @Test
    fun packetStream_accumulatesAndCreatesPacketsCorrectly() {
        val packetStrings = listOf("10 A0 01 00 F0", "10 A0 03 00 F0 F1 F2", "10 A0 02 00 F0 F1")

        val testSubscriber = packetStrings.reduceString().hexToByteArray().chunked(4)
            .toFlowable()
            .toPacketStream(ByteArrayToPacketAccumulator(packetParserMock))
            .testSubscribe()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().map { it.bytes }.toHexStrings().stripWhiteSpaceToLowercase())
            .containsExactlyElementsIn(packetStrings.stripWhiteSpaceToLowercase())
            .inOrder()
    }
}

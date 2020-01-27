package com.simprints.fingerprintscanner.v2.outgoing.main.packet

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.randomHollowPacketWithRawBytes
import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.common.syntax.setupMock
import com.simprints.testtools.common.syntax.whenThis
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class PacketDispatcherTest {

    private val packetSerializer = setupMock<PacketSerializer> {
        whenThis { serialize(anyNotNull()) } then {
            (it.arguments[0] as Packet).bytes
        }
    }

    @Test
    fun packetDispatcher_dispatch_streamsPacketsInCorrectOrder() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val packetDispatcher = PacketDispatcher(packetSerializer)
        packetDispatcher.connect(outputStream)

        val testSubscriber = inputStream.toFlowable().testSubscribe()

        val packets = List(20) { randomHollowPacketWithRawBytes() }
        packetDispatcher.dispatch(packets).test().await()

        outputStream.close()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().reduce { acc, bytes -> acc + bytes })
            .isEqualTo(packets.map { it.bytes }.reduce { acc, bytes -> acc + bytes })
    }
}

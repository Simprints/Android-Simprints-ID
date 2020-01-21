package com.simprints.fingerprintscanner.v2.incoming.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class RootMessageInputStreamTest {

    private val rootMessageAccumulator = RootResponseAccumulator(RootResponseParser())
    private val rootMessageInputStream = RootMessageInputStream(rootMessageAccumulator)

    @Test
    fun rootMessageInputStream_receiveRootResponse_correctlyForwardsResponse() {
        val messageBytes = "F0 10 00 00".hexToByteArray()
        val packets = messageBytes.chunked(2)
        val expectedResponse = EnterMainModeResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        rootMessageInputStream.connect(inputStream)

        val testSubscriber = rootMessageInputStream.receiveResponse<EnterMainModeResponse>().testSubscribe()

        packets.forEach { outputStream.write(it) }

        testSubscriber.awaitAndAssertSuccess()
        assertThat(testSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
    }

    @Test
    fun rootMessageInputStream_receiveMultipleOfSameResponses_forwardsOnlyFirstAsResponse() {
        val messageBytes = "F0 10 00 00 F0 10 00 00 F0 10 00 00".hexToByteArray()
        val packets = messageBytes.chunked(3)
        val expectedResponse = EnterMainModeResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        rootMessageInputStream.connect(inputStream)

        val testResponseSubscriber = rootMessageInputStream.receiveResponse<EnterMainModeResponse>().testSubscribe()
        val testStreamSubscriber = rootMessageInputStream.rootResponseStream.testSubscribe()

        packets.forEach { outputStream.write(it) }

        testResponseSubscriber.awaitAndAssertSuccess()
        testStreamSubscriber.awaitCount(3)
        assertThat(testResponseSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
        assertThat(testStreamSubscriber.valueCount()).isEqualTo(3)
    }

    @Test
    fun rootMessageInputStream_receiveDifferentResponses_forwardsOnlyCorrectResponse() {
        val messageBytes = "F0 20 00 00 F0 10 00 00 F0 30 00 00".hexToByteArray()
        val packets = messageBytes.chunked(3)
        val expectedResponse = EnterMainModeResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        rootMessageInputStream.connect(inputStream)

        val testResponseSubscriber = rootMessageInputStream.receiveResponse<EnterMainModeResponse>().testSubscribe()
        val testStreamSubscriber = rootMessageInputStream.rootResponseStream.testSubscribe()

        packets.forEach { outputStream.write(it) }

        testResponseSubscriber.awaitAndAssertSuccess()
        testStreamSubscriber.awaitCount(3)
        assertThat(testResponseSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
        assertThat(testStreamSubscriber.valueCount()).isEqualTo(3)
    }
}

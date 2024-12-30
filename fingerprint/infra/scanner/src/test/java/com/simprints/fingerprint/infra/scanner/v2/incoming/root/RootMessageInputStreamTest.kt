package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RootMessageInputStreamTest {
    private val rootMessageAccumulator = RootResponseAccumulator(RootResponseParser())
    private val rootMessageInputStream = RootMessageInputStream(rootMessageAccumulator)

    @Test
    fun rootMessageInputStream_receiveRootResponse_correctlyForwardsResponse() = runTest {
        val messageBytes = "F0 10 00 00".hexToByteArray()
        val packetsFlow = MutableSharedFlow<ByteArray>()
        val expectedResponse = EnterMainModeResponse()

        rootMessageInputStream.connect(packetsFlow)
        launch { packetsFlow.emitAll(messageBytes.chunked(2).asFlow()) }
        val testSubscriber = rootMessageInputStream.receiveResponse<EnterMainModeResponse>()
        assertThat(testSubscriber).isInstanceOf(expectedResponse::class.java)
    }

    @Test
    fun rootMessageInputStream_receiveMultipleOfSameResponses_forwardsOnlyFirstAsResponse() = runTest {
        val messageBytes = "F0 10 00 00 F0 10 00 00 F0 10 00 00".hexToByteArray()
        val packetsFlow = MutableSharedFlow<ByteArray>()

        val expectedResponse = EnterMainModeResponse()
        rootMessageInputStream.connect(packetsFlow)
        launch { packetsFlow.emitAll(messageBytes.chunked(3).asFlow()) }
        val testResponseSubscriber =
            rootMessageInputStream.receiveResponse<EnterMainModeResponse>()
        assertThat(testResponseSubscriber).isInstanceOf(expectedResponse::class.java)
    }

    @Test
    fun rootMessageInputStream_receiveDifferentResponses_forwardsOnlyCorrectResponse() = runTest {
        val messageBytes = "F0 20 00 00 F0 10 00 00 F0 30 00 00".hexToByteArray()
        val packetsFlow = MutableSharedFlow<ByteArray>()
        val expectedResponse = EnterMainModeResponse()
        rootMessageInputStream.connect(packetsFlow)
        launch { packetsFlow.emitAll(messageBytes.chunked(3).asFlow()) }
        val testResponseSubscriber =
            rootMessageInputStream.receiveResponse<EnterMainModeResponse>()
        assertThat(testResponseSubscriber).isInstanceOf(expectedResponse::class.java)
    }
}

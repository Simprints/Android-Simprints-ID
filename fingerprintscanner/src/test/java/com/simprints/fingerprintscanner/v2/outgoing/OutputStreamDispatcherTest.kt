package com.simprints.fingerprintscanner.v2.outgoing

import com.google.common.truth.Truth
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class OutputStreamDispatcherTest {

    @Test
    fun notConnected_callDispatch_throwsException() {
        val outputStreamDispatcher = OutputStreamDispatcher()
        assertThrows<IllegalStateException> {
            outputStreamDispatcher.dispatch(listOf(byteArrayOf(0x01, 0x02, 0x03))).blockingAwait()
        }
    }

    @Test
    fun connectedThenDisconnected_callDispatch_throwsException() {
        val outputStreamDispatcher = OutputStreamDispatcher()
        outputStreamDispatcher.connect(mock())
        outputStreamDispatcher.disconnect()
        assertThrows<IllegalStateException> {
            outputStreamDispatcher.dispatch(listOf(byteArrayOf(0x01, 0x02, 0x03))).blockingAwait()
        }
    }

    @Test
    fun connected_callDispatch_correctlySendsBytes() {
        val expectedBytes = listOf(byteArrayOf(0x01, 0x02, 0x03), byteArrayOf(0x04, 0x05), byteArrayOf(0x06))

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val outputStreamDispatcher = OutputStreamDispatcher()

        outputStreamDispatcher.connect(outputStream)

        val testSubscriber = inputStream.toFlowable().testSubscribe()

        outputStreamDispatcher.dispatch(expectedBytes).test().await()
        outputStream.close()

        testSubscriber.awaitCompletionWithNoErrors()

        Truth.assertThat(testSubscriber.values().reduce { acc, bytes -> acc + bytes })
            .isEqualTo(expectedBytes.reduce { acc, bytes -> acc + bytes })
    }
}

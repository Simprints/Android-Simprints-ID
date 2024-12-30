package com.simprints.fingerprint.infra.scanner.v2.outgoing

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.asFlow
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class OutputStreamDispatcherTest {
    @Test
    fun notConnected_callDispatch_throwsException() = runTest {
        val outputStreamDispatcher = OutputStreamDispatcher()
        assertThrows<IllegalStateException> {
            outputStreamDispatcher.dispatch(listOf(byteArrayOf(0x01, 0x02, 0x03)))
        }
    }

    @Test
    fun connectedThenDisconnected_callDispatch_throwsException() = runTest {
        val outputStreamDispatcher = OutputStreamDispatcher()
        outputStreamDispatcher.connect(mockk())
        outputStreamDispatcher.disconnect()
        assertThrows<IllegalStateException> {
            outputStreamDispatcher.dispatch(listOf(byteArrayOf(0x01, 0x02, 0x03)))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun connected_callDispatch_correctlySendsBytes() = runTest {
        val expectedBytes = listOf(byteArrayOf(0x01, 0x02, 0x03), byteArrayOf(0x04, 0x05), byteArrayOf(0x06))

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val outputStreamDispatcher = OutputStreamDispatcher()

        outputStreamDispatcher.connect(outputStream)

        val testFlow = inputStream.asFlow(UnconfinedTestDispatcher())

        outputStreamDispatcher.dispatch(expectedBytes)
        outputStream.close()
        Truth
            .assertThat(testFlow.toList().reduce { acc, bytes -> acc + bytes })
            .isEqualTo(expectedBytes.reduce { acc, bytes -> acc + bytes })
    }
}

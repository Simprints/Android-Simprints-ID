package com.simprints.fingerprint.infra.scanner.v2.outgoing.cypressota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands.DownloadCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.asFlow
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class CypressOtaMessageOutputStreamTest {
    private val mockCypressOtaMessageSerializer: CypressOtaMessageSerializer = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() = runTest {
        val message = DownloadCommand(5000)
        val expectedBytes = message.getBytes()
        every {
            mockCypressOtaMessageSerializer.serialize(eq(message))
        } returns listOf(expectedBytes)
        val messageOutputStream = CypressOtaMessageOutputStream(
            mockCypressOtaMessageSerializer,
            OutputStreamDispatcher(),
        )
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        messageOutputStream.connect(outputStream)

        val testSubscriber = inputStream.asFlow(UnconfinedTestDispatcher())

        messageOutputStream.sendMessage(message)
        outputStream.close()
        assertThat(testSubscriber.first()).isEqualTo(expectedBytes)
    }
}

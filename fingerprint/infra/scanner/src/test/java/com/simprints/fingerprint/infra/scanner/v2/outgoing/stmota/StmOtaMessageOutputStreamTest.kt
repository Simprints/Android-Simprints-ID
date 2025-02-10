package com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.asFlow
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class StmOtaMessageOutputStreamTest {
    private val mockStmOtaMessageSerializer: StmOtaMessageSerializer = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() = runTest {
        val message = WriteMemoryStartCommand()
        val expectedBytes = message.getBytes()
        every { mockStmOtaMessageSerializer.serialize(eq(message)) } returns listOf(expectedBytes)

        val messageOutputStream = StmOtaMessageOutputStream(
            mockStmOtaMessageSerializer,
            OutputStreamDispatcher(),
        )

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)
        messageOutputStream.connect(outputStream)
        val testSubscriber = inputStream.asFlow(UnconfinedTestDispatcher())
        messageOutputStream.sendMessage(message)
        outputStream.close()

        assertThat(testSubscriber.toList().first()).isEqualTo(expectedBytes)
    }
}

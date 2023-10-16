package com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.unit.reactive.testSubscribe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class StmOtaMessageOutputStreamTest {

    private val mockStmOtaMessageSerializer: StmOtaMessageSerializer = mockk()

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        val message = WriteMemoryStartCommand()
        val expectedBytes = message.getBytes()
        every { mockStmOtaMessageSerializer.serialize(eq(message)) } returns listOf(expectedBytes)

        val messageOutputStream = StmOtaMessageOutputStream(mockStmOtaMessageSerializer, OutputStreamDispatcher())

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        messageOutputStream.connect(outputStream)

        val testSubscriber = inputStream.toFlowable().testSubscribe()

        messageOutputStream.sendMessage(message).test().await()
        outputStream.close()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().first())
            .isEqualTo(expectedBytes)
    }
}

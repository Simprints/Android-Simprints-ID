package com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class StmOtaMessageOutputStreamTest {

    private val mockStmOtaMessageSerializer: StmOtaMessageSerializer = mock()

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        val message = WriteMemoryStartCommand()
        val expectedBytes = message.getBytes()
        whenever(mockStmOtaMessageSerializer) { serialize(eq(message)) } thenReturn listOf(expectedBytes)

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

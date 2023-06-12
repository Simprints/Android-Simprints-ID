package com.simprints.fingerprint.infra.scanner.v2.outgoing.cypressota

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands.DownloadCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class CypressOtaMessageOutputStreamTest {

    private val mockCypressOtaMessageSerializer: CypressOtaMessageSerializer = mock()

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        val message = DownloadCommand(5000)
        val expectedBytes = message.getBytes()
        whenever(mockCypressOtaMessageSerializer) { serialize(eq(message)) } thenReturn listOf(expectedBytes)

        val messageOutputStream = CypressOtaMessageOutputStream(mockCypressOtaMessageSerializer, OutputStreamDispatcher())

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

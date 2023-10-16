package com.simprints.fingerprint.infra.scanner.v2.outgoing.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.unit.reactive.testSubscribe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class RootMessageOutputStreamTest {

    private val rootMessageSerializerMock: RootMessageSerializer = mockk()

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        val message = EnterMainModeCommand()
        val expectedBytes = listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))
        every { rootMessageSerializerMock.serialize(eq(message)) } returns expectedBytes

        val messageOutputStream = RootMessageOutputStream(rootMessageSerializerMock, OutputStreamDispatcher())

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        messageOutputStream.connect(outputStream)

        val testSubscriber = inputStream.toFlowable().testSubscribe()

        messageOutputStream.sendMessage(message).test().await()
        outputStream.close()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().reduce { acc, bytes -> acc + bytes })
            .isEqualTo(expectedBytes.reduce { acc, bytes -> acc + bytes })
    }
}

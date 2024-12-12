package com.simprints.fingerprint.infra.scanner.v2.outgoing.main

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.unit.reactive.testSubscribe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class MainMessageOutputStreamTest {
    private val messageSerializerMock: MainMessageSerializer = mockk()
    private val outputStreamDispatcher: OutputStreamDispatcher = mockk()

    @Test
    fun messageOutputStream_connect_propagatesConnectMethodCorrectly() {
        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, outputStreamDispatcher)
        justRun { outputStreamDispatcher.connect(any()) }
        messageOutputStream.connect(mockk())

        verify { outputStreamDispatcher.connect(any()) }
    }

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageByCallingCorrectMethods() {
        every { messageSerializerMock.serialize(any()) } returns listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))
        every { outputStreamDispatcher.dispatch(any()) } returns Completable.complete()

        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, outputStreamDispatcher)

        messageOutputStream.sendMessage(mockk()).test().await()

        verify { messageSerializerMock.serialize(any()) }
        verify { outputStreamDispatcher.dispatch(any()) }
    }

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        val message = GetUn20OnCommand()
        val expectedBytes = listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))
        every { messageSerializerMock.serialize(eq(message)) } returns expectedBytes

        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, OutputStreamDispatcher())

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

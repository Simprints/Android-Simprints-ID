package com.simprints.fingerprintscanner.v2.outgoing.main

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.GetUn20OnCommand
import com.simprints.fingerprintscanner.v2.outgoing.OutputStreamDispatcher
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.Completable
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class MainMessageOutputStreamTest {

    private val messageSerializerMock: MainMessageSerializer = mock()
    private val outputStreamDispatcher: OutputStreamDispatcher = mock()

    @Test
    fun messageOutputStream_connect_propagatesConnectMethodCorrectly() {
        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, outputStreamDispatcher)

        messageOutputStream.connect(mock())

        verifyOnce(outputStreamDispatcher) { connect(anyNotNull()) }
    }

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageByCallingCorrectMethods() {
        whenever(messageSerializerMock) { serialize(anyNotNull()) } thenReturn listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))
        whenever(outputStreamDispatcher) { dispatch(anyNotNull()) } thenReturn Completable.complete()

        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, outputStreamDispatcher)

        messageOutputStream.sendMessage(mock()).test().await()

        verifyOnlyInteraction(messageSerializerMock) { serialize(anyNotNull()) }
        verifyOnce(outputStreamDispatcher) { dispatch(anyNotNull()) }
    }

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        val message = GetUn20OnCommand()
        val expectedBytes = listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))
        whenever(messageSerializerMock) { serialize(eq(message)) } thenReturn expectedBytes

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

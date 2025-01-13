package com.simprints.fingerprint.infra.scanner.v2.outgoing.main

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprint.infra.scanner.v2.tools.asFlow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class MainMessageOutputStreamTest {
    private val messageSerializerMock: MainMessageSerializer = mockk()
    private val outputStreamDispatcher: OutputStreamDispatcher = mockk {
        justRun { connect(any()) }
    }

    @Test
    fun messageOutputStream_connect_propagatesConnectMethodCorrectly() {
        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, outputStreamDispatcher)

        messageOutputStream.connect(mockk())

        verify { outputStreamDispatcher.connect(any()) }
    }

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageByCallingCorrectMethods() = runTest {
        every {
            messageSerializerMock.serialize(any())
        } returns listOf(
            byteArrayOf(
                0x10,
                0x20,
                0x30,
            ),
            byteArrayOf(0x40, 0x50),
        )
        coEvery {
            outputStreamDispatcher.dispatch(any())
        } just runs

        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, outputStreamDispatcher)

        messageOutputStream.sendMessage(mockk())

        verify { messageSerializerMock.serialize(any()) }
        coVerify { outputStreamDispatcher.dispatch(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() = runTest {
        val message = GetUn20OnCommand()
        val expectedBytes = listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))
        every { messageSerializerMock.serialize(eq(message)) } returns expectedBytes

        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, OutputStreamDispatcher())

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        messageOutputStream.connect(outputStream)

        val testSubscriber = inputStream.asFlow(UnconfinedTestDispatcher())

        messageOutputStream.sendMessage(message)
        outputStream.close()

        assertThat(
            testSubscriber.toList().reduce { acc, bytes -> acc + bytes },
        ).isEqualTo(expectedBytes.reduce { acc, bytes -> acc + bytes })
    }
}

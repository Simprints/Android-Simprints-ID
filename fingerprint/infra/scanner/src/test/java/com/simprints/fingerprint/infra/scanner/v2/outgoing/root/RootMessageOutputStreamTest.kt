package com.simprints.fingerprint.infra.scanner.v2.outgoing.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterMainModeCommand
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

class RootMessageOutputStreamTest {
    private val rootMessageSerializerMock: RootMessageSerializer = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() = runTest {
        val message = EnterMainModeCommand()
        val expectedBytes = listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))
        every { rootMessageSerializerMock.serialize(message) } returns expectedBytes

        val messageOutputStream =
            RootMessageOutputStream(rootMessageSerializerMock, OutputStreamDispatcher())

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        messageOutputStream.connect(outputStream)

        val testSubscriber = inputStream.asFlow(UnconfinedTestDispatcher())

        messageOutputStream.sendMessage(message)
        outputStream.close()

        assertThat(testSubscriber.toList().reduce { acc, bytes -> acc + bytes })
            .isEqualTo(expectedBytes.reduce { acc, bytes -> acc + bytes })
    }
}

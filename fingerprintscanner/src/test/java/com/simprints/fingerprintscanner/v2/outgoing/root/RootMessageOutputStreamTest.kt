package com.simprints.fingerprintscanner.v2.outgoing.root

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class RootMessageOutputStreamTest {

    private val rootMessageSerializerMock: RootMessageSerializer = mock()

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        val message = EnterMainModeCommand()
        val packets = listOf(byteArrayOf(0x10, 0x20, 0x30), byteArrayOf(0x40, 0x50))

        whenever(rootMessageSerializerMock) { serialize(eq(message)) } thenReturn packets

        val messageOutputStream = RootMessageOutputStream(rootMessageSerializerMock)

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        messageOutputStream.connect(outputStream)

        val testSubscriber = inputStream.toFlowable().testSubscribe()

        messageOutputStream.sendMessage(message).test().await()
        outputStream.close()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values().reduce { acc, bytes -> acc + bytes })
            .isEqualTo(packets.reduce { acc, bytes -> acc + bytes })
    }
}

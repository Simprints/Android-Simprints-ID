package com.simprints.fingerprintscanner.v2.outgoing

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.outgoing.main.MessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.message.MessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.main.packet.PacketDispatcher
import com.simprints.testtools.common.syntax.*
import io.reactivex.Completable
import org.junit.Test

class MessageOutputStreamTest {

    private val messageSerializerMock: MessageSerializer = mock()
    private val packetDispatcherMock: PacketDispatcher = mock()

    @Test
    fun messageOutputStream_connect_propagatesConnectMethodCorrectly() {
        val messageOutputStream = MessageOutputStream(messageSerializerMock, packetDispatcherMock)

        messageOutputStream.connect(mock())

        verifyOnce(packetDispatcherMock) { connect(anyNotNull()) }
    }

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        whenever(messageSerializerMock) { serialize(anyNotNull()) } thenReturn listOf(mock(), mock(), mock())
        whenever(packetDispatcherMock) { dispatch(anyNotNull()) } thenReturn Completable.complete()

        val messageOutputStream = MessageOutputStream(messageSerializerMock, packetDispatcherMock)

        messageOutputStream.sendMessage(object : OutgoingMessage {
            override fun getBytes(): ByteArray = byteArrayOf(0x10, 0x20, 0x30)
        }).test().await()

        verifyOnlyInteraction(messageSerializerMock) { serialize(anyNotNull()) }
        verifyOnlyInteraction(packetDispatcherMock) { dispatch(anyNotNull()) }
    }
}

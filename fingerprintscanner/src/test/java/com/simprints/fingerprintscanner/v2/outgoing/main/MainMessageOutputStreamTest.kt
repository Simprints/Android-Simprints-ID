package com.simprints.fingerprintscanner.v2.outgoing.main

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.outgoing.main.message.MainMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.main.packet.PacketDispatcher
import com.simprints.testtools.common.syntax.*
import io.reactivex.Completable
import org.junit.Test

class MainMessageOutputStreamTest {

    private val messageSerializerMock: MainMessageSerializer = mock()
    private val packetDispatcherMock: PacketDispatcher = mock()

    @Test
    fun messageOutputStream_connect_propagatesConnectMethodCorrectly() {
        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, packetDispatcherMock)

        messageOutputStream.connect(mock())

        verifyOnce(packetDispatcherMock) { connect(anyNotNull()) }
    }

    @Test
    fun messageOutputStream_sendMessage_serializesAndDispatchesMessageCorrectly() {
        whenever(messageSerializerMock) { serialize(anyNotNull()) } thenReturn listOf(mock(), mock(), mock())
        whenever(packetDispatcherMock) { dispatch(anyNotNull()) } thenReturn Completable.complete()

        val messageOutputStream = MainMessageOutputStream(messageSerializerMock, packetDispatcherMock)

        messageOutputStream.sendMessage(object : OutgoingMainMessage {
            override fun getBytes(): ByteArray = byteArrayOf(0x10, 0x20, 0x30)
        }).test().await()

        verifyOnlyInteraction(messageSerializerMock) { serialize(anyNotNull()) }
        verifyOnlyInteraction(packetDispatcherMock) { dispatch(anyNotNull()) }
    }
}

package com.simprints.fingerprintscanner.v2.scanner

import android.bluetooth.BluetoothSocket
import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.incoming.MessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.MessageOutputStream
import io.reactivex.Single

class Scanner(
    private val messageInputStream: MessageInputStream,
    private val messageOutputStream: MessageOutputStream
): Connectable {

    private lateinit var socket: BluetoothSocket

    override fun connect(socket: BluetoothSocket) {
        this.socket = socket
        this.socket.connect()
        messageInputStream.connect(socket.inputStream)
        messageOutputStream.connect(socket.outputStream)
    }

    override fun disconnect() {
        messageInputStream.disconnect()
        messageOutputStream.disconnect()
        socket.close()
    }

    private inline fun <reified R: IncomingMessage> sendCommandAndReceiveResponse(command: OutgoingMessage): Single<R> =
        messageOutputStream.sendMessage(command)
            .andThen(messageInputStream.recieveResponse())
}

package com.simprints.fingerprint.infra.scanner.v2.outgoing.common

import com.simprints.fingerprint.infra.scanner.v2.outgoing.OutgoingConnectable
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

/**
 * Class for sending any Iterable<ByteArray>, representing Bluetooth packets, out of the
 * [outputStream]
 */
class OutputStreamDispatcher @Inject constructor() : OutgoingConnectable {
    private var outputStream: OutputStream? = null

    override fun connect(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

    override fun disconnect() {
        outputStream = null
    }

    /**
     * @throws IOException if stream is broken
     * @throws IllegalStateException if using when not connected
     */
    fun dispatch(bytes: Iterable<ByteArray>) {
        bytes.forEach { dispatch(it) }
    }

    private fun dispatch(bytes: ByteArray) {
        outputStream?.let {
            it.write(bytes)
            it.flush()
        } ?: throw IllegalStateException("Trying to send bytes before connecting stream")
    }
}

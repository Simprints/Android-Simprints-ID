package com.simprints.fingerprintscanner.v2.outgoing

import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import io.reactivex.Completable
import java.io.IOException
import java.io.OutputStream

class OutputStreamDispatcher : OutgoingConnectable {

    private var outputStream: OutputStream? = null

    override fun connect(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

    override fun disconnect() {
    }

    /**
     * @throws IOException if stream is broken
     * @throws IllegalStateException if using when not connected
     */
    fun dispatch(bytes: Iterable<ByteArray>): Completable = completable {
        bytes.forEach { dispatch(it) }
    }

    private fun dispatch(bytes: ByteArray) {
        outputStream?.let {
            it.write(bytes)
            it.flush()
        } ?: throw IllegalStateException("Trying to send bytes before connecting stream")
    }
}

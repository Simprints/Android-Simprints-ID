package com.simprints.fingerprint.infra.scannermock.simulated.tools

import java.io.OutputStream

/**
 * Wraps an [OutputStream] and exposes an observable that contains the bytes written.
 * There is no byte buffer limit so onNext is called on [observers] only when flush() is called.
 */
class OutputStreamInterceptor : OutputStream() {
    val observers = mutableSetOf<(message: ByteArray) -> Unit>()

    private var buffer = mutableListOf<ByteArray>()

    override fun write(b: Int) {
        buffer.add(byteArrayOf(b.toByte()))
    }

    override fun write(b: ByteArray?) {
        if (b != null) buffer.add(b)
    }

    override fun write(
        b: ByteArray?,
        off: Int,
        len: Int,
    ) {
        if (b != null) buffer.add(b)
    }

    override fun flush() {
        val bytes = buffer.reduce { acc, bytes -> acc + bytes }
        observers.forEach { it(bytes) }
        buffer.clear()
    }
}

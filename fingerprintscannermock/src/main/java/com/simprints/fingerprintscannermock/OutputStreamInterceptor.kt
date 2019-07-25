package com.simprints.fingerprintscannermock

import com.simprints.fingerprintscannermock.ByteArrayUtils.concatenateByteArrays
import io.reactivex.Observer
import java.io.OutputStream

/**
 * Wraps an [OutputStream] and exposes an observable that contains the bytes written.
 * There is no byte buffer limit so [bytes] only emits the bytes when flush() is called.
 */
class OutputStreamInterceptor : OutputStream() {

    private var buffer = mutableListOf<ByteArray>()

    override fun write(b: Int) {
        buffer.add(byteArrayOf(b.toByte()))
    }

    override fun write(b: ByteArray?) {
        if (b != null) buffer.add(b)
    }

    override fun write(b: ByteArray?, off: Int, len: Int) {
        if (b != null) buffer.add(b)
    }

    override fun flush() {
        val bytes = concatenateByteArrays(buffer)
        observers.forEach { it.onNext(bytes) }
        buffer.clear()
    }

    override fun close() {
        observers.forEach { it.onComplete() }
    }

    companion object {
        val observers = mutableSetOf<Observer<ByteArray>>()
    }
}

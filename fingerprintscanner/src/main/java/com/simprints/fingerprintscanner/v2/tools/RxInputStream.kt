package com.simprints.fingerprintscanner.v2.tools

import io.reactivex.Flowable
import java.io.InputStream

fun InputStream.toFlowable(bufferSize: Int = 1024): Flowable<ByteArray> =
    Flowable.generate { emitter ->
        val buffer = ByteArray(bufferSize)
        val count = this.read(buffer)
        when {
            count == -1 -> emitter.onComplete()
            count < bufferSize -> emitter.onNext(buffer.copyOf(count))
            else -> emitter.onNext(buffer)
        }
    }

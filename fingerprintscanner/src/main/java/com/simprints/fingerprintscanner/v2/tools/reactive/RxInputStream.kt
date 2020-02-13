package com.simprints.fingerprintscanner.v2.tools.reactive

import io.reactivex.Flowable
import java.io.IOException
import java.io.InputStream

fun InputStream.toFlowable(bufferSize: Int = 1024): Flowable<ByteArray> =
    Flowable.generate { emitter ->
        try {
            val buffer = ByteArray(bufferSize)
            val count = this.read(buffer)
            when {
                count == -1 -> emitter.onComplete()
                count < bufferSize -> emitter.onNext(buffer.copyOf(count))
                else -> emitter.onNext(buffer)
            }
        } catch (e: IOException) {
            emitter.onError(e)
        }
    }

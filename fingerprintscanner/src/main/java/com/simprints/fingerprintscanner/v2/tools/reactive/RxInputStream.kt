package com.simprints.fingerprintscanner.v2.tools.reactive

import com.simprints.infra.logging.Simber
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
            // IOExceptions should be ignored because it  is
            // thrown when disconnecting the scanner and closing the input stream at the end of fingerprint collection process
            Simber.i(e)
        }
    }

package com.simprints.fingerprint.infra.scannermock.simulated.v1

import com.simprints.fingerprint.infra.scanner.v1.Message
import java.io.PipedInputStream
import java.io.PipedOutputStream

fun ByteArray.toMessageV1(): Message {
    val tempInputStream = PipedInputStream()
    val tempOutputStream = PipedOutputStream()
        .also {
            it.connect(tempInputStream)
            it.write(this)
            it.flush()
        }
    val message = Message.blockingReceiveFrom(tempInputStream, false)
    tempInputStream.close()
    tempOutputStream.close()
    return message
}

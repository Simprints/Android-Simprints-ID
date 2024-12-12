package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprint.infra.scanner.v2.outgoing.OutgoingConnectable
import io.reactivex.Flowable
import java.io.OutputStream

/**
 * Interface for a class that depends on both [IncomingConnectable]s and [OutgoingConnectable]s
 */
interface Connectable {
    fun connect(
        flowableInputStream: Flowable<ByteArray>,
        outputStream: OutputStream,
    )

    fun disconnect()
}

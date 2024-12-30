package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprint.infra.scanner.v2.outgoing.OutgoingConnectable
import kotlinx.coroutines.flow.Flow
import java.io.OutputStream

/**
 * Interface for a class that depends on both [IncomingConnectable]s and [OutgoingConnectable]s
 */
interface Connectable {
    fun connect(
        inputStreamFlow: Flow<ByteArray>,
        outputStream: OutputStream,
    )

    fun disconnect()
}

package com.simprints.fingerprint.infra.scanner.v2.outgoing

import java.io.OutputStream

/**
 * Interface for classes that handle the [OutputStream], sending bytes to the scanner.
 * Note that [connect] and [disconnect] do not change the state of the underlying [OutputStream] and
 * are to be used for setting-up and releasing resources appropriately.
 */
interface OutgoingConnectable {
    fun connect(outputStream: OutputStream)

    fun disconnect()
}

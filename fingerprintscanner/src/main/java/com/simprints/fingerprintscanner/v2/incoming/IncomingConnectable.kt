package com.simprints.fingerprintscanner.v2.incoming

import java.io.InputStream

/**
 * Interface for classes that handle the [InputStream], receiving bytes from the scanner.
 * Note that [connect] and [disconnect] do not change the state of the underlying [InputStream] and
 * are to be used for setting-up and releasing resources appropriately.
 */
interface IncomingConnectable {

    fun connect(inputStream: InputStream)
    fun disconnect()
}

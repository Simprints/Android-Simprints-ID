package com.simprints.fingerprint.infra.scanner.v2.incoming

import kotlinx.coroutines.flow.Flow
import java.io.InputStream

/**
 * Interface for classes that handle the [InputStream], receiving bytes from the scanner.
 * Note that [connect] and [disconnect] do not change the state of the underlying [InputStream] and
 * are to be used for setting-up and releasing resources appropriately.
 */
interface IncomingConnectable {
    fun connect(inputStreamFlow: Flow<ByteArray>)

    fun disconnect()
}

package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Takes an InputStream and transforms it into a Flow <RootResponse> for use while the Vero is in
 * Root Mode.
 */
class RootMessageInputStream @Inject constructor(
    private val rootResponseAccumulator: RootResponseAccumulator,
) : MessageInputStream {
    lateinit var rootResponseStream: Flow<RootResponse>

    override fun connect(inputStreamFlow: Flow<ByteArray>) {
        rootResponseStream = inputStreamFlow.toRootMessageStream(rootResponseAccumulator)
    }

    override fun disconnect() {
        rootResponseStream = emptyFlow()
    }

    suspend inline fun <reified R : RootResponse> receiveResponse(): R = rootResponseStream.filterIsInstance<R>().first()
}

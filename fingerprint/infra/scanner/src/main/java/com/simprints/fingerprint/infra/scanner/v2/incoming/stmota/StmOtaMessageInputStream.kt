package com.simprints.fingerprint.infra.scanner.v2.incoming.stmota

import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Takes an InputStream and transforms it into a Flow <StmOtaResponse> for use while the Vero is
 * in STM OTA Mode.
 */
class StmOtaMessageInputStream @Inject constructor(
    private val stmOtaResponseParser: StmOtaResponseParser,
) : MessageInputStream {
    lateinit var stmOtaResponseStream: Flow<StmOtaResponse>

    override fun connect(inputStreamFlow: Flow<ByteArray>) {
        stmOtaResponseStream = inputStreamFlow.map { stmOtaResponseParser.parse(it) }
    }

    override fun disconnect() {
        // No action needed as this stream is not usable anymore
    }

    suspend inline fun <reified R : StmOtaResponse> receiveResponse(): R = stmOtaResponseStream.filterIsInstance<R>().first()
}

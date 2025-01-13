package com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Takes an InputStream and transforms it into a Flow <CypressOtaResponse> for use while the Vero
 * is in Cypress OTA Mode.
 */
class CypressOtaMessageInputStream @Inject constructor(
    private val cypressOtaResponseParser: CypressOtaResponseParser,
) : MessageInputStream {
    lateinit var cypressOtaResponseStream: Flow<CypressOtaResponse>

    override fun connect(inputStreamFlow: Flow<ByteArray>) {
        cypressOtaResponseStream = inputStreamFlow.map { cypressOtaResponseParser.parse(it) }
    }

    override fun disconnect() {
        // No action needed as this stream is not usable anymore
    }

    suspend inline fun <reified R : CypressOtaResponse> receiveResponse(): R = cypressOtaResponseStream.filterIsInstance<R>().first()
}

package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.core.NonCancellableIO
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class CypressOtaMessageChannel @Inject constructor(
    incoming: CypressOtaMessageInputStream,
    outgoing: CypressOtaMessageOutputStream,
    @NonCancellableIO coroutineContext: CoroutineContext,
) : MessageChannel<CypressOtaMessageInputStream, CypressOtaMessageOutputStream>(
        incoming,
        outgoing,
        coroutineContext,
    ) {
    suspend inline fun <reified R : CypressOtaResponse> sendCommandAndReceiveResponse(command: CypressOtaCommand): R = runLockedTask {
        outgoing.sendMessage(command)
        incoming.receiveResponse<R>()
    }
}

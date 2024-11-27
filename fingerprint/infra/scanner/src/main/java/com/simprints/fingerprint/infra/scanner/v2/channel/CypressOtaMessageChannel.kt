package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.doSimultaneously
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await

class CypressOtaMessageChannel(
    incoming: CypressOtaMessageInputStream,
    outgoing: CypressOtaMessageOutputStream,
    dispatcher: CoroutineDispatcher
) : MessageChannel<CypressOtaMessageInputStream, CypressOtaMessageOutputStream>(
    incoming, outgoing, dispatcher
) {

    suspend inline fun <reified R : CypressOtaResponse> sendCommandAndReceiveResponse(
        command: CypressOtaCommand
    ): R = runLockedTask {
        outgoing.sendMessage(command).doSimultaneously(incoming.receiveResponse<R>()).await()
    }
}

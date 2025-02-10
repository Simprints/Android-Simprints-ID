package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.core.NonCancellableIO
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class StmOtaMessageChannel @Inject constructor(
    incoming: StmOtaMessageInputStream,
    outgoing: StmOtaMessageOutputStream,
    @NonCancellableIO coroutineContext: CoroutineContext,
) : MessageChannel<StmOtaMessageInputStream, StmOtaMessageOutputStream>(
        incoming,
        outgoing,
        coroutineContext,
    ) {
    suspend inline fun <reified R : StmOtaResponse> sendCommandAndReceiveResponse(command: StmOtaCommand): R = runLockedTask {
        outgoing.sendMessage(command)
        incoming.receiveResponse<R>()
    }

    suspend inline fun sendStmOtaModeCommand(command: StmOtaCommand) = runLockedTask {
        outgoing.sendMessage(command)
    }
}

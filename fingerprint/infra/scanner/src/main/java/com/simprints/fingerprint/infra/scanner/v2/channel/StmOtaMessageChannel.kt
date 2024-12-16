package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.doSimultaneously
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StmOtaMessageChannel @Inject constructor(
    incoming: StmOtaMessageInputStream,
    outgoing: StmOtaMessageOutputStream,
    @DispatcherIO dispatcher: CoroutineDispatcher,
) : MessageChannel<StmOtaMessageInputStream, StmOtaMessageOutputStream>(
        incoming,
        outgoing,
        dispatcher,
    ) {
    suspend inline fun <reified R : StmOtaResponse> sendCommandAndReceiveResponse(command: StmOtaCommand): R = runLockedTask {
        outgoing.sendMessage(command).doSimultaneously(incoming.receiveResponse<R>()).await()
    }

    suspend inline fun sendStmOtaModeCommand(command: StmOtaCommand) = runLockedTask {
        outgoing.sendMessage(command).await()
    }
}

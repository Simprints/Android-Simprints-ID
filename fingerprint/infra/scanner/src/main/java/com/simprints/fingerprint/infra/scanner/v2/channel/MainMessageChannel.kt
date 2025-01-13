package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.core.NonCancellableIO
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.main.MainMessageOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class MainMessageChannel @Inject constructor(
    incoming: MainMessageInputStream,
    outgoing: MainMessageOutputStream,
    @NonCancellableIO coroutineContext: CoroutineContext,
) : MessageChannel<MainMessageInputStream, MainMessageOutputStream>(
        incoming,
        outgoing,
        coroutineContext,
    ) {
    suspend inline fun <reified R : IncomingMainMessage> sendCommandAndReceiveResponse(command: OutgoingMainMessage): R = runLockedTask {
        outgoing.sendMessage(command)
        incoming.receiveResponse<R>()
    }

    suspend fun sendMainModeCommand(command: OutgoingMainMessage) = runLockedTask {
        outgoing.sendMessage(command)
    }

    suspend inline fun <reified R : IncomingMainMessage> receiveResponse() = runLockedTask {
        incoming.receiveResponse<R>()
    }
}

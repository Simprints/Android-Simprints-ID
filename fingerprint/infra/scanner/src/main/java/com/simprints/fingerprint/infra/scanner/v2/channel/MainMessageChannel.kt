package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.doSimultaneously
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainMessageChannel @Inject constructor(
    incoming: MainMessageInputStream,
    outgoing: MainMessageOutputStream,
    @DispatcherIO dispatcher: CoroutineDispatcher
) : MessageChannel<MainMessageInputStream, MainMessageOutputStream>(
    incoming, outgoing, dispatcher
) {

    suspend inline fun <reified R : IncomingMainMessage> sendCommandAndReceiveResponse(
        command: OutgoingMainMessage
    ): R = runLockedTask {
        outgoing.sendMessage(command).doSimultaneously(incoming.receiveResponse<R>()).await()
    }

    suspend fun sendMainModeCommand(command: OutgoingMainMessage) = runLockedTask {
        outgoing.sendMessage(command).await()
    }

    suspend inline fun <reified R : IncomingMainMessage> receiveResponse() = runLockedTask {
        incoming.receiveResponse<R>().await()
    }
}

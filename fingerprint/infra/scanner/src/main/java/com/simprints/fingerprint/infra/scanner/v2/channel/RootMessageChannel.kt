package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.doSimultaneously
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await

class RootMessageChannel(
    incoming: RootMessageInputStream,
    outgoing: RootMessageOutputStream,
    dispatcher: CoroutineDispatcher
) : MessageChannel<RootMessageInputStream, RootMessageOutputStream>(
    incoming, outgoing, dispatcher
) {

    suspend inline fun <reified R : RootResponse> sendCommandAndReceiveResponse(command: RootCommand): R =
        runLockedTask {
            outgoing.sendMessage(command).doSimultaneously(incoming.receiveResponse<R>()).await()
        }
}

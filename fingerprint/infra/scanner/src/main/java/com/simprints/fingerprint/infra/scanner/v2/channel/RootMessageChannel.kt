package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.doSimultaneously
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootMessageChannel @Inject constructor(
    incoming: RootMessageInputStream,
    outgoing: RootMessageOutputStream,
    @DispatcherIO dispatcher: CoroutineDispatcher
) : MessageChannel<RootMessageInputStream, RootMessageOutputStream>(
    incoming, outgoing, dispatcher
) {

    suspend inline fun <reified R : RootResponse> sendCommandAndReceiveResponse(command: RootCommand): R =
        runLockedTask {
            outgoing.sendMessage(command).doSimultaneously(incoming.receiveResponse<R>()).await()
        }
}

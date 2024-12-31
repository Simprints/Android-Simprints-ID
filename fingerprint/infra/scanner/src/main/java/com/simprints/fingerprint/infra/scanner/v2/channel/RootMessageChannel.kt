package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.core.NonCancellableIO
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.root.RootMessageOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class RootMessageChannel @Inject constructor(
    incoming: RootMessageInputStream,
    outgoing: RootMessageOutputStream,
    @NonCancellableIO coroutineContext: CoroutineContext,
) : MessageChannel<RootMessageInputStream, RootMessageOutputStream>(
        incoming,
        outgoing,
        coroutineContext,
    ) {
    suspend inline fun <reified R : RootResponse> sendCommandAndReceiveResponse(command: RootCommand): R = runLockedTask {
        outgoing.sendMessage(command)
        incoming.receiveResponse<R>()
    }
}

package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.doSimultaneously
import io.reactivex.Single

class RootMessageChannel(
    incoming: RootMessageInputStream,
    outgoing: RootMessageOutputStream
) : MessageChannel<RootMessageInputStream, RootMessageOutputStream>(incoming, outgoing) {

    inline fun <reified R : RootResponse> sendRootModeCommandAndReceiveResponse(command: RootCommand): Single<R> =
        outgoing.sendMessage(command)
            .doSimultaneously(incoming.receiveResponse())
}

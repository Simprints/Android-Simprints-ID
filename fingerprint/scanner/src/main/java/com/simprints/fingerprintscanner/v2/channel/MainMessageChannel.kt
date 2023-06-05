package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.doSimultaneously
import io.reactivex.Single

class MainMessageChannel(
    incoming: MainMessageInputStream,
    outgoing: MainMessageOutputStream
) : MessageChannel<MainMessageInputStream, MainMessageOutputStream>(incoming, outgoing) {

    inline fun <reified R : IncomingMainMessage> sendMainModeCommandAndReceiveResponse(command: OutgoingMainMessage): Single<R> =
        outgoing.sendMessage(command)
            .doSimultaneously(incoming.receiveResponse())
}

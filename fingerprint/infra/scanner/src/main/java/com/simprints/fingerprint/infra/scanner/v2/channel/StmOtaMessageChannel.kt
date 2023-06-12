package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.doSimultaneously
import io.reactivex.Single

class StmOtaMessageChannel(
    incoming: StmOtaMessageInputStream,
    outgoing: StmOtaMessageOutputStream
) : MessageChannel<StmOtaMessageInputStream, StmOtaMessageOutputStream>(incoming, outgoing) {

    inline fun <reified R : StmOtaResponse> sendStmOtaModeCommandAndReceiveResponse(command: StmOtaCommand): Single<R> =
        outgoing.sendMessage(command)
            .doSimultaneously(incoming.receiveResponse())
}

package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.tools.reactive.doSimultaneously
import io.reactivex.Single

class CypressOtaMessageChannel(
    incoming: CypressOtaMessageInputStream,
    outgoing: CypressOtaMessageOutputStream
) : MessageChannel<CypressOtaMessageInputStream, CypressOtaMessageOutputStream>(incoming, outgoing) {

    inline fun <reified R : CypressOtaResponse> sendCypressOtaModeCommandAndReceiveResponse(command: CypressOtaCommand): Single<R> =
        outgoing.sendMessage(command)
            .doSimultaneously(incoming.receiveResponse())
}

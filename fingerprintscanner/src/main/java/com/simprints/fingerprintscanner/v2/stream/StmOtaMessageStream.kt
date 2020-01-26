package com.simprints.fingerprintscanner.v2.stream

import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageOutputStream

class StmOtaMessageStream(
    incoming: StmOtaMessageInputStream,
    outgoing: StmOtaMessageOutputStream
) : MessageStream<StmOtaMessageInputStream, StmOtaMessageOutputStream>(incoming, outgoing)

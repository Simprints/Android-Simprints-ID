package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageOutputStream

class StmOtaMessageChannel(
    incoming: StmOtaMessageInputStream,
    outgoing: StmOtaMessageOutputStream
) : MessageChannel<StmOtaMessageInputStream, StmOtaMessageOutputStream>(incoming, outgoing)

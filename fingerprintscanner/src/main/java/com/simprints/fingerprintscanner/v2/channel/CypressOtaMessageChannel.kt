package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream

class CypressOtaMessageChannel(
    incoming: CypressOtaMessageInputStream,
    outgoing: CypressOtaMessageOutputStream
) : MessageChannel<CypressOtaMessageInputStream, CypressOtaMessageOutputStream>(incoming, outgoing)

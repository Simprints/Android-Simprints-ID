package com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroResponseParser

class VeroResponseAccumulator(veroResponseParser: VeroResponseParser) :
    PacketToMainMessageAccumulator<VeroResponse>(VeroMessageProtocol, veroResponseParser)

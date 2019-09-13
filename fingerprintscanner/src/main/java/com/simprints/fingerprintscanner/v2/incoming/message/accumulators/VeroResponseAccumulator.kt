package com.simprints.fingerprintscanner.v2.incoming.message.accumulators

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.incoming.message.parsers.VeroResponseParser

class VeroResponseAccumulator(veroResponseParser: VeroResponseParser) :
    PacketToMessageAccumulator<VeroResponse>(VeroMessageProtocol, veroResponseParser)

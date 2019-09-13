package com.simprints.fingerprintscanner.v2.incoming.message.accumulators

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.incoming.message.parsers.VeroEventParser

class VeroEventAccumulator(veroEventParser: VeroEventParser) :
    PacketToMessageAccumulator<VeroEvent>(VeroMessageProtocol, veroEventParser)

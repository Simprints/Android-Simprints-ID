package com.simprints.fingerprintscanner.v2.incoming.message.accumulators

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.incoming.message.parsers.Un20ResponseParser

class Un20ResponseAccumulator(un20ResponseParser: Un20ResponseParser) :
    PacketToMessageAccumulator<Un20Response>(Un20MessageProtocol, un20ResponseParser)

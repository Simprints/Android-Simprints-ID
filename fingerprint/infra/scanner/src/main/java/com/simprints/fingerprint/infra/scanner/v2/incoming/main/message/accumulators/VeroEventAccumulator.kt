package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.VeroEventParser

class VeroEventAccumulator(veroEventParser: VeroEventParser) :
    PacketToMainMessageAccumulator<VeroEvent>(VeroMessageProtocol, veroEventParser)

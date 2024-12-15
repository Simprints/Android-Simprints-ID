package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.VeroEventParser
import javax.inject.Inject

class VeroEventAccumulator @Inject constructor(
    veroEventParser: VeroEventParser,
) : PacketToMainMessageAccumulator<VeroEvent>(VeroMessageProtocol, veroEventParser)

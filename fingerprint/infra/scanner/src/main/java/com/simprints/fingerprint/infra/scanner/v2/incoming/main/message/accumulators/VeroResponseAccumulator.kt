package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.VeroResponseParser
import javax.inject.Inject

class VeroResponseAccumulator @Inject constructor(
    veroResponseParser: VeroResponseParser,
) : PacketToMainMessageAccumulator<VeroResponse>(VeroMessageProtocol, veroResponseParser)

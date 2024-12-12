package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.Un20ResponseParser
import javax.inject.Inject

class Un20ResponseAccumulator @Inject constructor(
    un20ResponseParser: Un20ResponseParser,
) : PacketToMainMessageAccumulator<Un20Response>(Un20MessageProtocol, un20ResponseParser)

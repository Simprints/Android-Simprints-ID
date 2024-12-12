package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol.HEADER_INDICES
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol.HEADER_SIZE
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol.getTotalLengthFromHeader
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.ByteArrayAccumulator
import javax.inject.Inject

class RootResponseAccumulator @Inject constructor(
    rootResponseParser: RootResponseParser,
) : ByteArrayAccumulator<ByteArray, RootResponse>(
        fragmentAsByteArray = { it },
        canComputeElementLength = { bytes -> bytes.size >= HEADER_SIZE },
        computeElementLength = { bytes -> getTotalLengthFromHeader(bytes.sliceArray(HEADER_INDICES)) },
        buildElement = { bytes -> rootResponseParser.parse(bytes) },
    )

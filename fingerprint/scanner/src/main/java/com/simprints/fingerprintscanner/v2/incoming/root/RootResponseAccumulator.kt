package com.simprints.fingerprintscanner.v2.incoming.root

import com.simprints.fingerprintscanner.v2.domain.root.RootMessageProtocol.HEADER_INDICES
import com.simprints.fingerprintscanner.v2.domain.root.RootMessageProtocol.HEADER_SIZE
import com.simprints.fingerprintscanner.v2.domain.root.RootMessageProtocol.getTotalLengthFromHeader
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.tools.accumulator.ByteArrayAccumulator

class RootResponseAccumulator(rootResponseParser: RootResponseParser) :
    ByteArrayAccumulator<ByteArray, RootResponse>(
        fragmentAsByteArray = { it },
        canComputeElementLength = { bytes -> bytes.size >= HEADER_SIZE },
        computeElementLength = { bytes -> getTotalLengthFromHeader(bytes.sliceArray(HEADER_INDICES)) },
        buildElement = { bytes -> rootResponseParser.parse(bytes) }
    )

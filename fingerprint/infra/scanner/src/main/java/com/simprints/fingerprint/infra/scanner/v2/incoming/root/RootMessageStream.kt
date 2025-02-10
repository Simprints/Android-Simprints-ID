package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.accumulateAndTakeElements
import kotlinx.coroutines.flow.Flow

fun Flow<ByteArray>.toRootMessageStream(accumulator: RootResponseAccumulator): Flow<RootResponse> = accumulateAndTakeElements(accumulator)

package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.accumulator.accumulateAndTakeElements
import io.reactivex.Flowable

fun Flowable<out ByteArray>.toRootMessageStream(accumulator: RootResponseAccumulator): Flowable<RootResponse> = this
    .accumulateAndTakeElements(accumulator)

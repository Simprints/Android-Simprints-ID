package com.simprints.fingerprintscanner.v2.incoming.root

import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.tools.accumulator.accumulateAndTakeElements
import io.reactivex.Flowable

fun Flowable<out ByteArray>.toRootMessageStream(accumulator: RootResponseAccumulator): Flowable<RootResponse> = this
    .accumulateAndTakeElements(accumulator)

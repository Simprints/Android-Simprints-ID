package com.simprints.fingerprint.capture.usecase

import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.FingerState
import javax.inject.Inject

internal class GetStartStateUseCase @Inject constructor() {
    operator fun invoke(fingerprintsToCapture: List<IFingerIdentifier>) = fingerprintsToCapture
        .groupingBy { it }
        .eachCount()
        .map { (id, quantity) -> FingerState(id, List(quantity) { CaptureState.NotCollected }) }
}

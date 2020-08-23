package com.simprints.fingerprint.activities.collect.domain

import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.FingerState
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

class StartingStateDeterminer {

    fun determineStartingFingerStates(fingerprintsToCapture: List<FingerIdentifier>): List<FingerState> {
        val quantities = mutableMapOf<FingerIdentifier, Int>()
        fingerprintsToCapture.forEach {
            quantities[it] = quantities[it]?.plus(1) ?: 1
        }
        return quantities.map { (id, quantity) ->
            FingerState(id, List(quantity) { CaptureState.NotCollected })
        }
    }
}

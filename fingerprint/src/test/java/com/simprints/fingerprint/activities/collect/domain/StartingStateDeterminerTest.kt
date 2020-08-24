package com.simprints.fingerprint.activities.collect.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.FingerState
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import org.junit.Test

class StartingStateDeterminerTest {

    private val startingStateDeterminer = StartingStateDeterminer()

    @Test
    fun singleCopies_determinesStartingStateCorrectly() {
        assertThat(startingStateDeterminer.determineStartingFingerStates(
            listOf(
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.RIGHT_5TH_FINGER,
                FingerIdentifier.LEFT_INDEX_FINGER
            )
        )).containsExactlyElementsIn(
            listOf(
                FingerState(FingerIdentifier.LEFT_THUMB, listOf(CaptureState.NotCollected)),
                FingerState(FingerIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(FingerIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected))
            ))
    }

    @Test
    fun multipleCopies_determinesStartingStateCorrectly() {
        assertThat(startingStateDeterminer.determineStartingFingerStates(
            listOf(
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.RIGHT_5TH_FINGER,
                FingerIdentifier.LEFT_INDEX_FINGER,
                FingerIdentifier.LEFT_INDEX_FINGER
            )
        )).containsExactlyElementsIn(
            listOf(
                FingerState(FingerIdentifier.LEFT_THUMB, listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected)),
                FingerState(FingerIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(FingerIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected))
            ))
    }

    @Test
    fun multipleCopiesDifferentOrder_determinesStartingStateCorrectly() {
        assertThat(startingStateDeterminer.determineStartingFingerStates(
            listOf(
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.RIGHT_5TH_FINGER,
                FingerIdentifier.LEFT_INDEX_FINGER,
                FingerIdentifier.LEFT_THUMB,
                FingerIdentifier.LEFT_INDEX_FINGER,
                FingerIdentifier.LEFT_THUMB
            )
        )).containsExactlyElementsIn(
            listOf(
                FingerState(FingerIdentifier.LEFT_THUMB, listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected)),
                FingerState(FingerIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(FingerIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected))
            ))
    }
}

package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.FingerState
import org.junit.Test

internal class GetStartStateUseCaseTest {
    private val getStartStateUseCase = GetStartStateUseCase()

    @Test
    fun singleCopies_determinesStartingStateCorrectly() {
        assertThat(
            getStartStateUseCase(
                listOf(
                    SampleIdentifier.LEFT_THUMB,
                    SampleIdentifier.RIGHT_5TH_FINGER,
                    SampleIdentifier.LEFT_INDEX_FINGER,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(SampleIdentifier.LEFT_THUMB, listOf(CaptureState.NotCollected)),
                FingerState(SampleIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(SampleIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected)),
            ),
        )
    }

    @Test
    fun multipleCopies_determinesStartingStateCorrectly() {
        assertThat(
            getStartStateUseCase(
                listOf(
                    SampleIdentifier.LEFT_THUMB,
                    SampleIdentifier.LEFT_THUMB,
                    SampleIdentifier.LEFT_THUMB,
                    SampleIdentifier.RIGHT_5TH_FINGER,
                    SampleIdentifier.LEFT_INDEX_FINGER,
                    SampleIdentifier.LEFT_INDEX_FINGER,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(
                    SampleIdentifier.LEFT_THUMB,
                    listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected),
                ),
                FingerState(SampleIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(SampleIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected)),
            ),
        )
    }

    @Test
    fun multipleCopiesDifferentOrder_determinesStartingStateCorrectly() {
        assertThat(
            getStartStateUseCase(
                listOf(
                    SampleIdentifier.LEFT_THUMB,
                    SampleIdentifier.RIGHT_5TH_FINGER,
                    SampleIdentifier.LEFT_INDEX_FINGER,
                    SampleIdentifier.LEFT_THUMB,
                    SampleIdentifier.LEFT_INDEX_FINGER,
                    SampleIdentifier.LEFT_THUMB,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(
                    SampleIdentifier.LEFT_THUMB,
                    listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected),
                ),
                FingerState(SampleIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(SampleIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected)),
            ),
        )
    }
}

package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
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
                    IFingerIdentifier.LEFT_THUMB,
                    IFingerIdentifier.RIGHT_5TH_FINGER,
                    IFingerIdentifier.LEFT_INDEX_FINGER,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(IFingerIdentifier.LEFT_THUMB, listOf(CaptureState.NotCollected)),
                FingerState(IFingerIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(IFingerIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected)),
            ),
        )
    }

    @Test
    fun multipleCopies_determinesStartingStateCorrectly() {
        assertThat(
            getStartStateUseCase(
                listOf(
                    IFingerIdentifier.LEFT_THUMB,
                    IFingerIdentifier.LEFT_THUMB,
                    IFingerIdentifier.LEFT_THUMB,
                    IFingerIdentifier.RIGHT_5TH_FINGER,
                    IFingerIdentifier.LEFT_INDEX_FINGER,
                    IFingerIdentifier.LEFT_INDEX_FINGER,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(
                    IFingerIdentifier.LEFT_THUMB,
                    listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected),
                ),
                FingerState(IFingerIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(IFingerIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected)),
            ),
        )
    }

    @Test
    fun multipleCopiesDifferentOrder_determinesStartingStateCorrectly() {
        assertThat(
            getStartStateUseCase(
                listOf(
                    IFingerIdentifier.LEFT_THUMB,
                    IFingerIdentifier.RIGHT_5TH_FINGER,
                    IFingerIdentifier.LEFT_INDEX_FINGER,
                    IFingerIdentifier.LEFT_THUMB,
                    IFingerIdentifier.LEFT_INDEX_FINGER,
                    IFingerIdentifier.LEFT_THUMB,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(
                    IFingerIdentifier.LEFT_THUMB,
                    listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected),
                ),
                FingerState(IFingerIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(IFingerIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected)),
            ),
        )
    }
}

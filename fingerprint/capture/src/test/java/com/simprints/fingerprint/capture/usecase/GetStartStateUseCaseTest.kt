package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.TemplateIdentifier
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
                    TemplateIdentifier.LEFT_THUMB,
                    TemplateIdentifier.RIGHT_5TH_FINGER,
                    TemplateIdentifier.LEFT_INDEX_FINGER,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(TemplateIdentifier.LEFT_THUMB, listOf(CaptureState.NotCollected)),
                FingerState(TemplateIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(TemplateIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected)),
            ),
        )
    }

    @Test
    fun multipleCopies_determinesStartingStateCorrectly() {
        assertThat(
            getStartStateUseCase(
                listOf(
                    TemplateIdentifier.LEFT_THUMB,
                    TemplateIdentifier.LEFT_THUMB,
                    TemplateIdentifier.LEFT_THUMB,
                    TemplateIdentifier.RIGHT_5TH_FINGER,
                    TemplateIdentifier.LEFT_INDEX_FINGER,
                    TemplateIdentifier.LEFT_INDEX_FINGER,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(
                    TemplateIdentifier.LEFT_THUMB,
                    listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected),
                ),
                FingerState(TemplateIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(TemplateIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected)),
            ),
        )
    }

    @Test
    fun multipleCopiesDifferentOrder_determinesStartingStateCorrectly() {
        assertThat(
            getStartStateUseCase(
                listOf(
                    TemplateIdentifier.LEFT_THUMB,
                    TemplateIdentifier.RIGHT_5TH_FINGER,
                    TemplateIdentifier.LEFT_INDEX_FINGER,
                    TemplateIdentifier.LEFT_THUMB,
                    TemplateIdentifier.LEFT_INDEX_FINGER,
                    TemplateIdentifier.LEFT_THUMB,
                ),
            ),
        ).containsExactlyElementsIn(
            listOf(
                FingerState(
                    TemplateIdentifier.LEFT_THUMB,
                    listOf(CaptureState.NotCollected, CaptureState.NotCollected, CaptureState.NotCollected),
                ),
                FingerState(TemplateIdentifier.RIGHT_5TH_FINGER, listOf(CaptureState.NotCollected)),
                FingerState(TemplateIdentifier.LEFT_INDEX_FINGER, listOf(CaptureState.NotCollected, CaptureState.NotCollected)),
            ),
        )
    }
}

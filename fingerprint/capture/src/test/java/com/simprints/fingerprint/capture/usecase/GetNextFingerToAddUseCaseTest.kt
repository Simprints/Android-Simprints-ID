package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.sample.SampleIdentifier
import org.junit.Test

class GetNextFingerToAddUseCaseTest {
    private val useCase = GetNextFingerToAddUseCase()

    @Test
    fun `Returns left thumb as first in priority`() {
        assertThat(useCase(listOf())).isEqualTo(SampleIdentifier.LEFT_THUMB)
    }

    @Test
    fun `Returns right thumb as if left thumb and index fingers already taken`() {
        assertThat(
            useCase(
                listOf(
                    SampleIdentifier.LEFT_THUMB,
                    SampleIdentifier.LEFT_INDEX_FINGER,
                ),
            ),
        ).isEqualTo(SampleIdentifier.RIGHT_THUMB)
    }
}

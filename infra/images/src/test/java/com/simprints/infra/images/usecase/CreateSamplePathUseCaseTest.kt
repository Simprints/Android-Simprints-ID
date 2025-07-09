package com.simprints.infra.images.usecase

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.models.GeneralConfiguration
import org.junit.Before
import org.junit.Test

class CreateSamplePathUseCaseTest {
    private lateinit var useCase: CreateSamplePathUseCase

    @Before
    fun setUp() {
        useCase = CreateSamplePathUseCase()
    }

    @Test
    fun `invoke should create a valid path for face sample`() {
        val expectedPath = "sessions/sessionId/faces/captureEventId.jpg"
        val result = useCase(
            sessionId = "sessionId",
            modality = GeneralConfiguration.Modality.FACE,
            sampleId = "captureEventId",
            fileExtension = "jpg",
        )

        assertThat(result.compose()).isEqualTo(expectedPath)
    }

    @Test
    fun `invoke should create a valid path for fingerprint sample`() {
        val expectedPath = "sessions/sessionId/fingerprints/captureEventId.swq"

        val result = useCase(
            sessionId = "sessionId",
            modality = GeneralConfiguration.Modality.FINGERPRINT,
            sampleId = "captureEventId",
            fileExtension = "swq",
        )

        assertThat(result.compose()).isEqualTo(expectedPath)
    }
}

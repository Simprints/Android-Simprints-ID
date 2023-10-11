package com.simprints.feature.orchestrator.usecases

import android.os.Parcelable
import com.google.common.truth.Truth.assertThat
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.matcher.FaceMatchResult
import com.simprints.feature.enrollast.EnrolLastBiometricResult
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import org.junit.Before
import org.junit.Test

class MapStepsForLastBiometricEnrolUseCaseTest {

    private lateinit var useCase: MapStepsForLastBiometricEnrolUseCase

    @Before
    fun setUp() {
        useCase = MapStepsForLastBiometricEnrolUseCase()
    }

    @Test
    fun `maps EnrolLastBiometricRequest correctly`() {
        val result = useCase(listOf<Parcelable>(
            EnrolLastBiometricResult("subjectId")
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.EnrolLastBiometricsResult("subjectId"))
    }


    @Test
    fun `maps FaceMatchResponse correctly`() {
        val result = useCase(listOf(
            FaceMatchResult(emptyList())
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FaceMatchResult(emptyList()))
    }

    @Test
    fun `maps mapFaceCaptureResult correctly`() {
        val result = useCase(listOf(
            FaceCaptureResult(
                results = listOf(
                    FaceCaptureResult.Item(0, null),
                    FaceCaptureResult.Item(0, FaceCaptureResult.Sample(
                        faceId = "faceId",
                        template = byteArrayOf(),
                        imageRef = null,
                        format = "format"
                    ))
                ),
            )
        ))

        assertThat(result.first()).isEqualTo(EnrolLastBiometricStepResult.FaceCaptureResult(listOf(
            FaceTemplateCaptureResult(
                template = byteArrayOf(),
                format = "format",
            )
        )))
    }

    // TODO add fingerprint tests
}

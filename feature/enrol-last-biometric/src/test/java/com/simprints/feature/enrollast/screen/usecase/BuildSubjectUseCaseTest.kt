package com.simprints.feature.enrollast.screen.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class BuildSubjectUseCaseTest {

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: BuildSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() }.returns(1L)

        useCase = BuildSubjectUseCase(timeHelper)
    }

    @Test
    fun `has no samples if no steps provided`() {
        val result = useCase(createParams(emptyList()))

        assertThat(result.fingerprintSamples).isEmpty()
        assertThat(result.faceSamples).isEmpty()
    }

    @Test
    fun `has no samples if no valid steps provided`() {
        val result = useCase(createParams(listOf(
            EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null),
            EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList()),
            EnrolLastBiometricStepResult.FaceMatchResult(emptyList()),
        )))

        assertThat(result.fingerprintSamples).isEmpty()
        assertThat(result.faceSamples).isEmpty()
    }

    @Test
    fun `maps first available fingerprint capture step results`() {
        val result = useCase(createParams(listOf(
            EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList()),
            EnrolLastBiometricStepResult.FingerprintCaptureResult(listOf(mockFingerprintResults(Finger.RIGHT_THUMB))),
            EnrolLastBiometricStepResult.FingerprintCaptureResult(listOf(mockFingerprintResults(Finger.LEFT_THUMB))),
        )))

        assertThat(result.fingerprintSamples).isNotEmpty()
        assertThat(result.fingerprintSamples.first().fingerIdentifier).isEqualTo(IFingerIdentifier.RIGHT_THUMB)
    }

    @Test
    fun `maps all provided fingerprint capture samples`() {
        val result = useCase(createParams(listOf(EnrolLastBiometricStepResult.FingerprintCaptureResult(listOf(
            mockFingerprintResults(Finger.RIGHT_5TH_FINGER),
            mockFingerprintResults(Finger.RIGHT_4TH_FINGER),
            mockFingerprintResults(Finger.RIGHT_3RD_FINGER),
            mockFingerprintResults(Finger.RIGHT_INDEX_FINGER),
            mockFingerprintResults(Finger.RIGHT_THUMB),
            mockFingerprintResults(Finger.LEFT_THUMB),
            mockFingerprintResults(Finger.LEFT_INDEX_FINGER),
            mockFingerprintResults(Finger.LEFT_3RD_FINGER),
            mockFingerprintResults(Finger.LEFT_4TH_FINGER),
            mockFingerprintResults(Finger.LEFT_5TH_FINGER),
        )))))

        assertThat(result.fingerprintSamples).isNotEmpty()
        assertThat(result.fingerprintSamples.size).isEqualTo(10)
    }

    @Test
    fun `maps first available face capture step results`() {
        val result = useCase(createParams(listOf(
            EnrolLastBiometricStepResult.FaceMatchResult(emptyList()),
            EnrolLastBiometricStepResult.FaceCaptureResult(mockFaceResultsList("first")),
            EnrolLastBiometricStepResult.FaceCaptureResult(mockFaceResultsList("second")),
        )))

        assertThat(result.faceSamples).isNotEmpty()
        assertThat(result.faceSamples.first().format).isEqualTo("first")
    }

    private fun createParams(steps: List<EnrolLastBiometricStepResult>) = EnrolLastBiometricParams(
        projectId = PROJECT_ID,
        userId = USER_ID,
        moduleId = MODULE_ID,
        steps = steps,
    )

    private fun mockFingerprintResults(finger: Finger) =
        FingerTemplateCaptureResult(finger, byteArrayOf(), 1, FingerprintTemplateFormat.ISO_19794_2)

    private fun mockFaceResultsList(format: String) =
        listOf(FaceTemplateCaptureResult(byteArrayOf(), format))

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val USER_ID = "userId"
        private const val MODULE_ID = "moduleId"
    }
}
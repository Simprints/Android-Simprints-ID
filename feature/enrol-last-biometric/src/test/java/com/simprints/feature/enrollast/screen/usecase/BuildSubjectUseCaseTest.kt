package com.simprints.feature.enrollast.screen.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class BuildSubjectUseCaseTest {
    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: BuildSubjectUseCase

    private lateinit var subjectFactory: SubjectFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() }.returns(Timestamp(1L))
        subjectFactory = SubjectFactory(
            encodingUtils = EncodingUtilsImplForTests,
            timeHelper = timeHelper,
        )
        useCase = BuildSubjectUseCase(timeHelper = timeHelper, subjectFactory = subjectFactory)
    }

    @Test
    fun `has no samples if no steps provided`() {
        val result = useCase(createParams(emptyList()))

        assertThat(result.fingerprintSamples).isEmpty()
        assertThat(result.faceSamples).isEmpty()
    }

    @Test
    fun `has no samples if no valid steps provided`() {
        val result = useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null),
                    EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList(), mockk()),
                    EnrolLastBiometricStepResult.FaceMatchResult(emptyList()),
                ),
            ),
        )

        assertThat(result.fingerprintSamples).isEmpty()
        assertThat(result.faceSamples).isEmpty()
    }

    @Test
    fun `maps first available fingerprint capture step results`() {
        val result = useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList(), mockk()),
                    EnrolLastBiometricStepResult.FingerprintCaptureResult(
                        REFERENCE_ID,
                        listOf(mockFingerprintResults(Finger.RIGHT_THUMB)),
                    ),
                    EnrolLastBiometricStepResult.FingerprintCaptureResult(
                        REFERENCE_ID,
                        listOf(mockFingerprintResults(Finger.LEFT_THUMB)),
                    ),
                ),
            ),
        )

        assertThat(result.fingerprintSamples).isNotEmpty()
        assertThat(result.fingerprintSamples.first().fingerIdentifier).isEqualTo(IFingerIdentifier.RIGHT_THUMB)
    }

    @Test
    fun `maps all provided fingerprint capture samples`() {
        val result = useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.FingerprintCaptureResult(
                        REFERENCE_ID,
                        listOf(
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
                        ),
                    ),
                ),
            ),
        )

        assertThat(result.fingerprintSamples).isNotEmpty()
        assertThat(result.fingerprintSamples.size).isEqualTo(10)
    }

    @Test
    fun `maps first available face capture step results`() {
        val result = useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.FaceMatchResult(emptyList()),
                    EnrolLastBiometricStepResult.FaceCaptureResult(REFERENCE_ID, mockFaceResultsList("first")),
                    EnrolLastBiometricStepResult.FaceCaptureResult(REFERENCE_ID, mockFaceResultsList("second")),
                ),
            ),
        )

        assertThat(result.faceSamples).isNotEmpty()
        assertThat(result.faceSamples.first().format).isEqualTo("first")
    }

    private fun createParams(steps: List<EnrolLastBiometricStepResult>) = EnrolLastBiometricParams(
        projectId = PROJECT_ID,
        userId = USER_ID,
        moduleId = MODULE_ID,
        steps = steps,
    )

    private fun mockFingerprintResults(finger: Finger) = FingerTemplateCaptureResult(finger, byteArrayOf(), 1, "ISO_19794_2")

    private fun mockFaceResultsList(format: String) = listOf(FaceTemplateCaptureResult(byteArrayOf(), format))

    companion object {
        private const val REFERENCE_ID = "referenceId"

        private const val PROJECT_ID = "projectId"
        private val USER_ID = "userId".asTokenizableRaw()
        private val MODULE_ID = "moduleId".asTokenizableRaw()
    }
}

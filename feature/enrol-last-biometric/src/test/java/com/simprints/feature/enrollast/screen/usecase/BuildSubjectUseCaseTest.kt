package com.simprints.feature.enrollast.screen.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
                    EnrolLastBiometricStepResult.FaceMatchResult(emptyList(), mockk()),
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
                    EnrolLastBiometricStepResult.CaptureResult(
                        REFERENCE_ID,
                        listOf(mockFingerprintResults(SampleIdentifier.RIGHT_THUMB)),
                    ),
                    EnrolLastBiometricStepResult.CaptureResult(
                        REFERENCE_ID,
                        listOf(mockFingerprintResults(SampleIdentifier.LEFT_THUMB)),
                    ),
                ),
            ),
        )

        assertThat(result.fingerprintSamples).isNotEmpty()
        assertThat(result.fingerprintSamples.first().identifier).isEqualTo(SampleIdentifier.RIGHT_THUMB)
    }

    @Test
    fun `maps all provided fingerprint capture samples`() {
        val result = useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.CaptureResult(
                        REFERENCE_ID,
                        listOf(
                            mockFingerprintResults(SampleIdentifier.RIGHT_5TH_FINGER),
                            mockFingerprintResults(SampleIdentifier.RIGHT_4TH_FINGER),
                            mockFingerprintResults(SampleIdentifier.RIGHT_3RD_FINGER),
                            mockFingerprintResults(SampleIdentifier.RIGHT_INDEX_FINGER),
                            mockFingerprintResults(SampleIdentifier.RIGHT_THUMB),
                            mockFingerprintResults(SampleIdentifier.LEFT_THUMB),
                            mockFingerprintResults(SampleIdentifier.LEFT_INDEX_FINGER),
                            mockFingerprintResults(SampleIdentifier.LEFT_3RD_FINGER),
                            mockFingerprintResults(SampleIdentifier.LEFT_4TH_FINGER),
                            mockFingerprintResults(SampleIdentifier.LEFT_5TH_FINGER),
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
                    EnrolLastBiometricStepResult.FaceMatchResult(emptyList(), mockk()),
                    EnrolLastBiometricStepResult.CaptureResult(REFERENCE_ID, listOf(mockFaceResults("first"))),
                    EnrolLastBiometricStepResult.CaptureResult(REFERENCE_ID, listOf(mockFaceResults("second"))),
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

    private fun mockFingerprintResults(finger: SampleIdentifier) = CaptureSample(
        captureEventId = "eventId",
        identifier = finger,
        template = byteArrayOf(),
        format = "ISO_19794_2",
        modality = Modality.FINGERPRINT,
    )

    private fun mockFaceResults(format: String) = CaptureSample(
        captureEventId = "eventId",
        template = byteArrayOf(),
        format = format,
        modality = Modality.FACE,
    )

    companion object {
        private const val REFERENCE_ID = "referenceId"

        private const val PROJECT_ID = "projectId"
        private val USER_ID = "userId".asTokenizableRaw()
        private val MODULE_ID = "moduleId".asTokenizableRaw()
    }
}

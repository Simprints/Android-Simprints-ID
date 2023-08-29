package com.simprints.feature.enrollast.screen.usecase

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.eventsync.sync.down.tasks.SubjectFactory
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

class BuildSubjectUseCaseTest {

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var subjectFactory: SubjectFactory

    private lateinit var useCase: BuildSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(UUID::class)

        every { timeHelper.now() } returns TIME_NOW
        every { UUID.randomUUID() } answers {
            mockk {
                every { this@mockk.toString() } returns RANDOM_ID
            }
        }
        useCase = BuildSubjectUseCase(timeHelper = timeHelper, subjectFactory = subjectFactory)
    }

    private fun verifySubjectFactoryCall(
        fingerprintSamples: () -> List<FingerprintSample> = { emptyList() },
        faceSamples: () -> List<FaceSample> = { emptyList() }
    ) {
        coVerify {
            subjectFactory.buildEncryptedSubject(
                subjectId = RANDOM_ID,
                projectId = PROJECT_ID,
                attendantId = USER_ID,
                moduleId = MODULE_ID,
                createdAt = Date(TIME_NOW),
                updatedAt = null,
                fingerprintSamples = fingerprintSamples(),
                faceSamples = faceSamples()
            )
        }
    }

    @Test
    fun `has no samples if no steps provided`() = runTest {
        useCase(createParams(emptyList()))

        verifySubjectFactoryCall(
            fingerprintSamples = { emptyList() },
            faceSamples = { emptyList() }
        )
    }

    @Test
    fun `has no samples if no valid steps provided`() = runTest {
        useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null),
                    EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList()),
                    EnrolLastBiometricStepResult.FaceMatchResult(emptyList()),
                )
            )
        )

        verifySubjectFactoryCall(
            fingerprintSamples = { emptyList() },
            faceSamples = { emptyList() }
        )
    }

    @Test
    fun `maps first available fingerprint capture step results`() = runTest {
        useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.FingerprintMatchResult(emptyList()),
                    EnrolLastBiometricStepResult.FingerprintCaptureResult(
                        listOf(mockFingerprintResults(Finger.RIGHT_THUMB))
                    ),
                    EnrolLastBiometricStepResult.FingerprintCaptureResult(
                        listOf(mockFingerprintResults(Finger.LEFT_THUMB))
                    ),
                )
            )
        )

        verifySubjectFactoryCall(
            fingerprintSamples = {
                listOf(
                    FingerprintSample(
                        id = RANDOM_ID,
                        fingerIdentifier = IFingerIdentifier.RIGHT_THUMB,
                        template = byteArrayOf(),
                        templateQualityScore = 1,
                        format = FORMAT
                    )
                )
            }
        )
    }

    @Test
    fun `maps all provided fingerprint capture samples`() = runTest {
        val fingerprints = listOf(
            Finger.RIGHT_5TH_FINGER,
            Finger.RIGHT_4TH_FINGER,
            Finger.RIGHT_3RD_FINGER,
            Finger.RIGHT_INDEX_FINGER,
            Finger.RIGHT_THUMB,
            Finger.LEFT_THUMB,
            Finger.LEFT_INDEX_FINGER,
            Finger.LEFT_3RD_FINGER,
            Finger.LEFT_4TH_FINGER,
            Finger.LEFT_5TH_FINGER
        )
        val expected = fingerprints.map {
            FingerprintSample(
                id = RANDOM_ID,
                fingerIdentifier = fromDomainToModuleApi(it),
                template = byteArrayOf(),
                templateQualityScore = 1,
                format = FORMAT
            )
        }
        useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.FingerprintCaptureResult(
                        fingerprints.map(::mockFingerprintResults)
                    )
                )
            )
        )

        verifySubjectFactoryCall(
            fingerprintSamples = { expected },
        )
    }

    @Test
    fun `maps first available face capture step results`() = runTest {
        val formatFirst = "formatFirst"
        val formatSecond = "formatSecond"
        useCase(
            createParams(
                listOf(
                    EnrolLastBiometricStepResult.FaceMatchResult(emptyList()),
                    EnrolLastBiometricStepResult.FaceCaptureResult(mockFaceResultsList(formatFirst)),
                    EnrolLastBiometricStepResult.FaceCaptureResult(mockFaceResultsList(formatSecond)),
                )
            )
        )

        verifySubjectFactoryCall(
            faceSamples = {
                listOf(
                    FaceSample(
                        template = byteArrayOf(),
                        format = formatFirst,
                        id = RANDOM_ID
                    )
                )
            }
        )
    }

    private fun fromDomainToModuleApi(finger: Finger) = when (finger) {
        Finger.RIGHT_5TH_FINGER -> IFingerIdentifier.RIGHT_5TH_FINGER
        Finger.RIGHT_4TH_FINGER -> IFingerIdentifier.RIGHT_4TH_FINGER
        Finger.RIGHT_3RD_FINGER -> IFingerIdentifier.RIGHT_3RD_FINGER
        Finger.RIGHT_INDEX_FINGER -> IFingerIdentifier.RIGHT_INDEX_FINGER
        Finger.RIGHT_THUMB -> IFingerIdentifier.RIGHT_THUMB
        Finger.LEFT_THUMB -> IFingerIdentifier.LEFT_THUMB
        Finger.LEFT_INDEX_FINGER -> IFingerIdentifier.LEFT_INDEX_FINGER
        Finger.LEFT_3RD_FINGER -> IFingerIdentifier.LEFT_3RD_FINGER
        Finger.LEFT_4TH_FINGER -> IFingerIdentifier.LEFT_4TH_FINGER
        Finger.LEFT_5TH_FINGER -> IFingerIdentifier.LEFT_5TH_FINGER
    }

    private fun createParams(steps: List<EnrolLastBiometricStepResult>) = EnrolLastBiometricParams(
        projectId = PROJECT_ID,
        userId = USER_ID,
        moduleId = MODULE_ID,
        steps = steps,
    )

    private fun mockFingerprintResults(finger: Finger) =
        FingerTemplateCaptureResult(finger, byteArrayOf(), 1, FORMAT)

    private fun mockFaceResultsList(format: String) =
        listOf(FaceTemplateCaptureResult(byteArrayOf(), format))

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val USER_ID = "userId"
        private const val MODULE_ID = "moduleId"
        private const val RANDOM_ID = "RANDOM_ID"
        private const val TIME_NOW = 1L
        private const val FORMAT = "ISO_19794_2"
    }
}

package com.simprints.feature.enrollast.screen.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.externalcredential.ExternalCredentialMapper
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.infra.eventsync.sync.common.EnrolmentRecordFactory
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BuildRecordUseCaseTest {
    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var credentialSearchResult: ExternalCredentialSearchResult.Complete

    @MockK
    private lateinit var credentialMapper: ExternalCredentialMapper

    private lateinit var useCase: BuildRecordUseCase

    private lateinit var enrolmentRecordFactory: EnrolmentRecordFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { timeHelper.now() }.returns(Timestamp(1L))
        enrolmentRecordFactory = EnrolmentRecordFactory(
            encodingUtils = EncodingUtilsImplForTests,
            timeHelper = timeHelper,
        )
        useCase = BuildRecordUseCase(
            timeHelper = timeHelper,
            enrolmentRecordFactory = enrolmentRecordFactory,
            credentialMapper = credentialMapper,
        )
    }

    @Test
    fun `has no references if no steps provided`() = runTest {
        val result =
            useCase(
                createParams(steps = emptyList(), credentialSearchResult = credentialSearchResult),
                isAddingCredential = false,
            )

        assertThat(result.references).isEmpty()
    }

    @Test
    fun `has no references if no valid steps provided`() = runTest {
        val result = useCase(
            createParams(
                steps = listOf(
                    EnrolLastBiometricStepResult.EnrolLastBiometricsResult(null),
                    EnrolLastBiometricStepResult.MatchResult(emptyList(), mockk()),
                    EnrolLastBiometricStepResult.MatchResult(emptyList(), mockk()),
                ),
                credentialSearchResult = credentialSearchResult,
            ),
            isAddingCredential = false,
        )

        assertThat(result.references).isEmpty()
    }

    @Test
    fun `maps first available fingerprint capture step results`() = runTest {
        val result = useCase(
            params = createParams(
                steps = listOf(
                    EnrolLastBiometricStepResult.MatchResult(emptyList(), mockk()),
                    EnrolLastBiometricStepResult.CaptureResult(
                        BiometricReferenceCapture(
                            referenceId = REFERENCE_ID,
                            modality = Modality.FINGERPRINT,
                            format = "ISO_19794_2",
                            templates = listOf(mockFingerprintResults(TemplateIdentifier.RIGHT_THUMB)),
                        ),
                    ),
                    EnrolLastBiometricStepResult.CaptureResult(
                        BiometricReferenceCapture(
                            referenceId = REFERENCE_ID,
                            modality = Modality.FINGERPRINT,
                            format = "ISO_19794_2",
                            templates = listOf(mockFingerprintResults(TemplateIdentifier.LEFT_THUMB)),
                        ),
                    ),
                ),
                credentialSearchResult = credentialSearchResult,
            ),
            isAddingCredential = false,
        )

        assertThat(result.references).isNotEmpty()
        assertThat(
            result.references
                .first()
                .templates
                .first()
                .identifier,
        ).isEqualTo(TemplateIdentifier.RIGHT_THUMB)
    }

    @Test
    fun `maps all provided fingerprint capture samples`() = runTest {
        val result = useCase(
            createParams(
                steps = listOf(
                    EnrolLastBiometricStepResult.CaptureResult(
                        BiometricReferenceCapture(
                            referenceId = REFERENCE_ID,
                            modality = Modality.FINGERPRINT,
                            format = "ISO_19794_2",
                            templates = listOf(
                                mockFingerprintResults(TemplateIdentifier.RIGHT_5TH_FINGER),
                                mockFingerprintResults(TemplateIdentifier.RIGHT_4TH_FINGER),
                                mockFingerprintResults(TemplateIdentifier.RIGHT_3RD_FINGER),
                                mockFingerprintResults(TemplateIdentifier.RIGHT_INDEX_FINGER),
                                mockFingerprintResults(TemplateIdentifier.RIGHT_THUMB),
                                mockFingerprintResults(TemplateIdentifier.LEFT_THUMB),
                                mockFingerprintResults(TemplateIdentifier.LEFT_INDEX_FINGER),
                                mockFingerprintResults(TemplateIdentifier.LEFT_3RD_FINGER),
                                mockFingerprintResults(TemplateIdentifier.LEFT_4TH_FINGER),
                                mockFingerprintResults(TemplateIdentifier.LEFT_5TH_FINGER),
                            ),
                        ),
                    ),
                ),
                credentialSearchResult = credentialSearchResult,
            ),
            isAddingCredential = false,
        )

        assertThat(result.references.size).isEqualTo(1)
        assertThat(
            result.references
                .first()
                .templates.size,
        ).isEqualTo(10)
    }

    @Test
    fun `maps first available face capture step results`() = runTest {
        val result = useCase(
            params = createParams(
                listOf(
                    EnrolLastBiometricStepResult.MatchResult(emptyList(), mockk()),
                    EnrolLastBiometricStepResult.CaptureResult(
                        BiometricReferenceCapture(
                            referenceId = REFERENCE_ID,
                            modality = Modality.FACE,
                            format = "first",
                            templates = listOf(mockFaceResults()),
                        ),
                    ),
                    EnrolLastBiometricStepResult.CaptureResult(
                        BiometricReferenceCapture(
                            referenceId = REFERENCE_ID,
                            modality = Modality.FACE,
                            format = "second",
                            templates = listOf(mockFaceResults()),
                        ),
                    ),
                ),
                credentialSearchResult = credentialSearchResult,
            ),
            isAddingCredential = false,
        )

        assertThat(result.references).isNotEmpty()
        assertThat(result.references.first().format).isEqualTo("first")
    }

    @Test
    fun `includes external credential when isAddingCredential is true and scannedCredential is not null`() = runTest {
        val mockTokenized = mockk<TokenizableString.Tokenized>()
        val mockCredentialType = mockk<ExternalCredentialType>()
        val mockExternalCredential = mockk<com.simprints.core.domain.externalcredential.ExternalCredential> {
            every { value } returns mockTokenized
            every { type } returns mockCredentialType
        }

        coEvery { credentialMapper.mapExternalCredential(credentialSearchResult, any()) } returns mockExternalCredential

        val result =
            useCase(
                createParams(steps = emptyList(), credentialSearchResult = credentialSearchResult),
                isAddingCredential = true,
            )

        assertThat(result.externalCredentials).hasSize(1)
        assertThat(result.externalCredentials.first().value).isEqualTo(mockTokenized)
        assertThat(result.externalCredentials.first().type).isEqualTo(mockCredentialType)
    }

    @Test
    fun `has no external credentials when isAddingCredential is true but scannedCredential is null`() = runTest {
        val result =
            useCase(createParams(steps = emptyList(), credentialSearchResult = null), isAddingCredential = true)

        assertThat(result.externalCredentials).isEmpty()
    }

    @Test
    fun `has no external credentials when isAddingCredential is false even if credentialSearchResult is not null`() = runTest {
        val result =
            useCase(
                createParams(steps = emptyList(), credentialSearchResult = credentialSearchResult),
                isAddingCredential = false,
            )

        assertThat(result.externalCredentials).isEmpty()
    }

    private fun createParams(
        steps: List<EnrolLastBiometricStepResult>,
        credentialSearchResult: ExternalCredentialSearchResult.Complete?,
    ) = EnrolLastBiometricParams(
        projectId = PROJECT_ID,
        userId = USER_ID,
        moduleId = MODULE_ID,
        steps = steps,
        credentialSearchResult = credentialSearchResult,
    )

    private fun mockFingerprintResults(finger: TemplateIdentifier) = BiometricTemplateCapture(
        captureEventId = "eventId",
        identifier = finger,
        template = byteArrayOf(),
    )

    private fun mockFaceResults() = BiometricTemplateCapture(
        captureEventId = "eventId",
        template = byteArrayOf(),
    )

    companion object {
        private const val REFERENCE_ID = "referenceId"

        private const val PROJECT_ID = "projectId"
        private val USER_ID = "userId".asTokenizableRaw()
        private val MODULE_ID = "moduleId".asTokenizableRaw()
    }
}

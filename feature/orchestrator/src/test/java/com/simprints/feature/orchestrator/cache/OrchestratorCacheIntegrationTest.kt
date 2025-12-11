package com.simprints.feature.orchestrator.cache

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.BiometricTemplateCapture
import com.simprints.core.domain.reference.TemplateIdentifier
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.face.capture.FaceCaptureParams
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.consent.ConsentParams
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.exitform.ExitFormOption
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.BoundingBox
import com.simprints.feature.externalcredential.model.CredentialMatch
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.fetchsubject.FetchSubjectParams
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.LoginResult
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.selectsubject.SelectSubjectParams
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.setup.SetupResult
import com.simprints.feature.validatepool.ValidateSubjectPoolFragmentParams
import com.simprints.feature.validatepool.ValidateSubjectPoolResult
import com.simprints.fingerprint.capture.FingerprintCaptureParams
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.matching.MatchResult
import com.simprints.infra.security.SecurityManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrchestratorCacheIntegrationTest {
    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var prefs: SharedPreferences

    private var jsonHelper = JsonHelper

    private lateinit var cache: OrchestratorCache

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns prefs

        // Mock prefs set and get behaviour
        val stringSlot = slot<String>()
        justRun { prefs.edit().putString(any(), capture(stringSlot)).commit() }
        every { prefs.getString(any(), any()) } answers { stringSlot.captured }

        cache = OrchestratorCache(
            securityManager,
            jsonHelper,
        )
    }

    @Test
    fun `Stores and restores common steps`() {
        val expected = listOf(
            Step(
                id = StepId.SETUP,
                navigationActionId = 5,
                destinationId = 6,
                params = null,
                status = StepStatus.IN_PROGRESS,
                result = SetupResult(true),
            ),
            Step(
                id = StepId.FETCH_GUID,
                navigationActionId = 5,
                destinationId = 6,
                params = FetchSubjectParams("projectId", "subjectId", ""),
                status = StepStatus.COMPLETED,
                result = FetchSubjectResult(false),
            ),
            Step(
                id = StepId.CONSENT,
                navigationActionId = 5,
                destinationId = 6,
                params = ConsentParams(type = ConsentType.ENROL),
                status = StepStatus.COMPLETED,
                result = ConsentResult(true),
            ),
            Step(
                id = StepId.ENROL_LAST_BIOMETRIC,
                navigationActionId = 5,
                destinationId = 6,
                params = EnrolLastBiometricParams(
                    projectId = "projectId",
                    userId = TokenizableString.Raw("value"),
                    moduleId = TokenizableString.Raw("value"),
                    steps = listOf(
                        EnrolLastBiometricStepResult.CaptureResult(
                            BiometricReferenceCapture(
                                referenceId = "referenceId",
                                modality = Modality.FINGERPRINT,
                                format = "format",
                                templates = listOf(
                                    BiometricTemplateCapture(
                                        captureEventId = GUID1,
                                        template = BiometricTemplate(
                                            identifier = TemplateIdentifier.LEFT_THUMB,
                                            template = byteArrayOf(1, 2, 3),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                        EnrolLastBiometricStepResult.MatchResult(
                            listOf(MatchComparisonResult("subjectId", 0.5f)),
                            FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                        ),
                        EnrolLastBiometricStepResult.MatchResult(
                            listOf(MatchComparisonResult("subjectId", 0.5f)),
                            FaceConfiguration.BioSdk.RANK_ONE,
                        ),
                        EnrolLastBiometricStepResult.EnrolLastBiometricsResult("subjectId"),
                    ),
                    scannedCredential = null,
                ),
                status = StepStatus.COMPLETED,
                result = ValidateSubjectPoolResult(true),
            ),
            Step(
                id = StepId.CONFIRM_IDENTITY,
                navigationActionId = 5,
                destinationId = 6,
                params = SelectSubjectParams("projectId", "subjectId", null),
                status = StepStatus.COMPLETED,
                result = SelectSubjectResult(true, savedCredential = null),
            ),
            Step(
                id = StepId.VALIDATE_ID_POOL,
                navigationActionId = 5,
                destinationId = 6,
                params = ValidateSubjectPoolFragmentParams(SubjectQuery()),
                status = StepStatus.COMPLETED,
                result = ValidateSubjectPoolResult(true),
            ),
            Step(
                id = StepId.SELECT_SUBJECT_AGE,
                navigationActionId = 5,
                destinationId = 6,
                params = null,
                status = StepStatus.COMPLETED,
                result = SelectSubjectAgeGroupResult(AgeGroup(10, null)),
            ),
            Step(
                id = StepId.EXTERNAL_CREDENTIAL,
                navigationActionId = 5,
                destinationId = 6,
                params = ExternalCredentialParams(
                    subjectId = "subjectId",
                    flowType = FlowType.IDENTIFY,
                    ageGroup = AgeGroup(1, 2),
                    probeReferences = listOf(
                        BiometricReferenceCapture(
                            referenceId = "referenceId1",
                            modality = Modality.FINGERPRINT,
                            format = "format",
                            templates = listOf(
                                BiometricTemplateCapture(
                                    captureEventId = "captureEvent1",
                                    template = BiometricTemplate(
                                        identifier = TemplateIdentifier.LEFT_THUMB,
                                        template = byteArrayOf(1, 2, 3),
                                    ),
                                ),
                            ),
                        ),
                        BiometricReferenceCapture(
                            referenceId = "referenceId1",
                            modality = Modality.FACE,
                            format = "format2",
                            templates = listOf(
                                BiometricTemplateCapture(
                                    captureEventId = "captureEvent2",
                                    template = BiometricTemplate(
                                        template = byteArrayOf(2, 3, 4),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
                status = StepStatus.COMPLETED,
                result = ExternalCredentialSearchResult(
                    flowType = FlowType.IDENTIFY,
                    scannedCredential = ScannedCredential(
                        credentialScanId = "scanId",
                        credential = "credential".asTokenizableEncrypted(),
                        credentialType = ExternalCredentialType.GhanaIdCard,
                        documentImagePath = "image/path.jpg",
                        zoomedCredentialImagePath = "image/path.jpg",
                        credentialBoundingBox = BoundingBox(0, 1, 2, 3),
                        scanStartTime = Timestamp(1L),
                        scanEndTime = Timestamp(2L, false, 123L),
                        scannedValue = "credential".asTokenizableRaw(),
                    ),
                    matchResults = listOf(
                        CredentialMatch(
                            credential = "credential".asTokenizableEncrypted(),
                            matchResult = MatchComparisonResult("subjectId", 0.5f),
                            verificationThreshold = 55f,
                            bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                        ),
                    ),
                ),
            ),
        )

        cache.steps = expected
        val actual = cache.steps

        assertThat(actual).hasSize(expected.size)
        for (i in expected.indices) {
            compareStubs(expected[i], actual[i])
        }
    }

    @Test
    fun `Stores and restores fingerprint modality steps`() {
        val expected = listOf(
            Step(
                id = StepId.FINGERPRINT_CAPTURE,
                navigationActionId = 3,
                destinationId = 4,
                params = FingerprintCaptureParams(
                    flowType = FlowType.ENROL,
                    fingerprintsToCapture = listOf(TemplateIdentifier.LEFT_4TH_FINGER),
                    fingerprintSDK = FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
                status = StepStatus.COMPLETED,
                result = BiometricReferenceCapture(
                    "",
                    modality = Modality.FINGERPRINT,
                    format = "format",
                    templates = listOf(
                        BiometricTemplateCapture(
                            captureEventId = GUID1,
                            template = BiometricTemplate(
                                identifier = TemplateIdentifier.LEFT_THUMB,
                                template = byteArrayOf(1, 2, 3),
                            ),
                        ),
                    ),
                ),
            ),
            Step(
                id = StepId.FINGERPRINT_MATCHER,
                navigationActionId = 3,
                destinationId = 4,
                params = MatchParams(
                    flowType = FlowType.IDENTIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.CommCare("name"),
                    bioSdk = FingerprintConfiguration.BioSdk.NEC,
                    probeReference = BiometricReferenceCapture(
                        referenceId = GUID1,
                        modality = Modality.FINGERPRINT,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "captureEvent1",
                                template = BiometricTemplate(
                                    identifier = TemplateIdentifier.LEFT_THUMB,
                                    template = byteArrayOf(1, 2, 3),
                                ),
                            ),
                        ),
                    ),
                ),
                status = StepStatus.COMPLETED,
                result = MatchResult(
                    listOf(MatchComparisonResult("subjectId", 0.5f)),
                    FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
            ),
        )

        cache.steps = expected
        val actual = cache.steps

        assertThat(actual).hasSize(expected.size)
        for (i in expected.indices) {
            compareStubs(expected[i], actual[i])
        }
    }

    @Test
    fun `Stores and restores face modality steps`() {
        val expected = listOf(
            Step(
                id = StepId.FACE_CAPTURE,
                navigationActionId = 5,
                destinationId = 6,
                params = FaceCaptureParams(3, FaceConfiguration.BioSdk.RANK_ONE),
                status = StepStatus.COMPLETED,
                result = BiometricReferenceCapture(
                    "",
                    modality = Modality.FACE,
                    format = "ROC",
                    templates = listOf(
                        BiometricTemplateCapture(
                            captureEventId = GUID1,
                            template = BiometricTemplate(
                                identifier = TemplateIdentifier.LEFT_THUMB,
                                template = byteArrayOf(1, 2, 3),
                            ),
                        ),
                    ),
                ),
            ),
            Step(
                id = StepId.FACE_MATCHER,
                navigationActionId = 3,
                destinationId = 4,
                params = MatchParams(
                    flowType = FlowType.IDENTIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                    bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                    probeReference = BiometricReferenceCapture(
                        referenceId = GUID1,
                        modality = Modality.FACE,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "captureEvent1",
                                template = BiometricTemplate(
                                    template = byteArrayOf(1, 2, 3),
                                ),
                            ),
                        ),
                    ),
                ),
                status = StepStatus.COMPLETED,
                result = MatchResult(
                    listOf(MatchComparisonResult("subjectId", 0.5f)),
                    FaceConfiguration.BioSdk.RANK_ONE,
                ),
            ),
        )

        cache.steps = expected
        val actual = cache.steps

        assertThat(actual).hasSize(expected.size)
        for (i in expected.indices) {
            compareStubs(expected[i], actual[i])
        }
    }

    @Test
    fun `Stores and restores exception steps`() {
        val expected = listOf(
            Step(
                id = 1,
                navigationActionId = 5,
                destinationId = 6,
                params = LoginParams("projectId", TokenizableString.Tokenized("value")),
                status = StepStatus.NOT_STARTED,
                result = LoginResult(false, LoginError.LoginNotCompleted),
            ),
            Step(
                id = 2,
                navigationActionId = 5,
                destinationId = 6,
                status = StepStatus.NOT_STARTED,
                result = AlertResult("key", AppErrorReason.UNEXPECTED_ERROR),
            ),
            Step(
                id = 3,
                navigationActionId = 5,
                destinationId = 6,
                status = StepStatus.NOT_STARTED,
                result = ExitFormResult(true, ExitFormOption.DataConcerns),
            ),
        )

        cache.steps = expected
        val actual = cache.steps

        assertThat(actual).hasSize(expected.size)
        for (i in expected.indices) {
            compareStubs(expected[i], actual[i])
        }
    }

    private fun compareStubs(
        expected: Step,
        actual: Step,
    ) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.navigationActionId).isEqualTo(expected.navigationActionId)
        assertThat(actual.destinationId).isEqualTo(expected.destinationId)
        assertThat(actual.status).isEqualTo(expected.status)
        assertThat(actual.result).isEqualTo(expected.result)
        assertThat(actual.params).isEqualTo(expected.params)
    }
}

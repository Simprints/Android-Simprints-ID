package com.simprints.feature.orchestrator.cache

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.json.JsonHelper
import com.simprints.face.capture.FaceCaptureParams
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.consent.ConsentParams
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.consent.ConsentType
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.enrollast.FaceTemplateCaptureResult
import com.simprints.feature.enrollast.FingerTemplateCaptureResult
import com.simprints.feature.enrollast.MatchResult
import com.simprints.feature.exitform.ExitFormOption
import com.simprints.feature.exitform.ExitFormResult
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
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.security.SecurityManager
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.FingerprintMatchResult
import com.simprints.matcher.MatchParams
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
                        EnrolLastBiometricStepResult.FingerprintCaptureResult(
                            "referenceId",
                            listOf(
                                FingerTemplateCaptureResult(
                                    Finger.LEFT_4TH_FINGER,
                                    byteArrayOf(1, 2, 3),
                                    10,
                                    "NEC",
                                ),
                            ),
                        ),
                        EnrolLastBiometricStepResult.FingerprintMatchResult(
                            listOf(MatchResult("subjectId", 0.5f)),
                            FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                        ),
                        EnrolLastBiometricStepResult.FaceCaptureResult(
                            "referenceId",
                            listOf(
                                FaceTemplateCaptureResult(byteArrayOf(1, 2, 3), "RankOne"),
                            ),
                        ),
                        EnrolLastBiometricStepResult.FaceMatchResult(
                            listOf(MatchResult("subjectId", 0.5f)),
                            FaceConfiguration.BioSdk.RANK_ONE,
                        ),
                        EnrolLastBiometricStepResult.EnrolLastBiometricsResult("subjectId"),
                    ),
                ),
                status = StepStatus.COMPLETED,
                result = ValidateSubjectPoolResult(true),
            ),
            Step(
                id = StepId.CONFIRM_IDENTITY,
                navigationActionId = 5,
                destinationId = 6,
                params = SelectSubjectParams("projectId", "subjectId"),
                status = StepStatus.COMPLETED,
                result = SelectSubjectResult(true),
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
                    fingerprintsToCapture = listOf(IFingerIdentifier.LEFT_4TH_FINGER),
                    fingerprintSDK = FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER,
                ),
                status = StepStatus.COMPLETED,
                result = FingerprintCaptureResult(
                    "",
                    results = listOf(
                        FingerprintCaptureResult.Item(
                            captureEventId = GUID1,
                            identifier = IFingerIdentifier.LEFT_THUMB,
                            sample = FingerprintCaptureResult.Sample(
                                fingerIdentifier = IFingerIdentifier.LEFT_4TH_FINGER,
                                template = byteArrayOf(1, 2, 3),
                                templateQualityScore = 10,
                                imageRef = SecuredImageRef(Path("file/path")),
                                format = "NEC",
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
                    probeReferenceId = GUID1,
                    flowType = FlowType.IDENTIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.CommCare("name"),
                    probeFingerprintSamples = listOf(
                        MatchParams.FingerprintSample(
                            fingerId = IFingerIdentifier.LEFT_4TH_FINGER,
                            format = "NEC",
                            template = byteArrayOf(1, 2, 3),
                        ),
                    ),
                ),
                status = StepStatus.COMPLETED,
                result = FingerprintMatchResult(
                    listOf(FingerprintMatchResult.Item("subjectId", 0.5f)),
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
                result = FaceCaptureResult(
                    "",
                    results = listOf(
                        FaceCaptureResult.Item(
                            captureEventId = "event",
                            index = 0,
                            sample = FaceCaptureResult.Sample(
                                faceId = "faceId",
                                template = byteArrayOf(1, 2, 3),
                                imageRef = SecuredImageRef(Path("file/path")),
                                format = "ROC",
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
                    probeReferenceId = GUID1,
                    flowType = FlowType.IDENTIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                    probeFaceSamples = listOf(
                        MatchParams.FaceSample(
                            faceId = "faceId",
                            template = byteArrayOf(1, 2, 3),
                        ),
                    ),
                ),
                status = StepStatus.COMPLETED,
                result = FaceMatchResult(
                    listOf(FaceMatchResult.Item("subjectId", 0.5f)),
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

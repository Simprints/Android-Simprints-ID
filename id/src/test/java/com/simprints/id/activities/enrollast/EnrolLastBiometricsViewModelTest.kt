package com.simprints.id.activities.enrollast

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.exceptions.validator.EnrolmentEventValidatorException
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Failed
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Success
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.exceptions.safe.DuplicateBiometricEnrolmentCheckFailed
import com.simprints.id.exceptions.unexpected.MissingCaptureResponse
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DecisionPolicy
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.logging.Simber
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EnrolLastBiometricsViewModelTest {

    companion object {
        private val fingerprintConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
        private val faceConfidenceDecisionPolicy = DecisionPolicy(15, 30, 40)
    }

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val stepsWithLastEnrolBiometrics = listOf(
        Step(
            GUID1,
            0,
            "activity_name",
            "key",
            mockk<EnrolLastBiometricsRequest>(),
            EnrolLastBiometricsResponse(GUID1),
            COMPLETED
        )
    )

    private val appRequestWithPastEnrolLastBiometricSteps = EnrolLastBiometricsRequest(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        DEFAULT_MODULE_ID,
        stepsWithLastEnrolBiometrics,
        GUID1
    )
    private val appRequestWithoutPastEnrolLastBiometricSteps = EnrolLastBiometricsRequest(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        DEFAULT_MODULE_ID,
        emptyList(),
        GUID1
    )


    private val generalConfiguration = mockk<GeneralConfiguration>()
    private val timeHelper = mockk<TimeHelper>(relaxed = true)
    private val enrolHelper = mockk<EnrolmentHelper>()
    private val configManager = mockk<ConfigManager>()

    private val viewModel = EnrolLastBiometricsViewModel(enrolHelper, timeHelper, configManager)

    @Before
    fun setUp() {
        mockkObject(Simber)
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns generalConfiguration
            every { fingerprint } returns mockk {
                every { decisionPolicy } returns fingerprintConfidenceDecisionPolicy
            }
            every { face } returns mockk {
                every { decisionPolicy } returns faceConfidenceDecisionPolicy
            }
        }
    }

    @Test
    fun getNextStep_enrolFails_shouldProduceFailedState() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns false
            val request = mockk<EnrolLastBiometricsRequest>()
            every { request.previousSteps } throws Throwable("No steps from previous run")
            viewModel.processEnrolLastBiometricsRequest(request)
            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
        }
    }

    @Test
    fun getNextStep_enrolAlreadyHappened_shouldProduceSuccessState() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns false
            viewModel.processEnrolLastBiometricsRequest(appRequestWithPastEnrolLastBiometricSteps)

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(GUID1)
            }
        }
    }

    @Test
    fun getNextStep_enrolNeverHappened_shouldProduceSuccessState() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns false
            val newEnrolment = mockk<Subject>()
            every {
                enrolHelper.buildSubject(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns newEnrolment

            viewModel.processEnrolLastBiometricsRequest(appRequestWithoutPastEnrolLastBiometricSteps)

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.subjectId)
            }
        }
    }

    @Test
    fun fingerprintWithEnrolmentPlus_processRequestWithHighConfidenceInPreviousSteps_shouldFail() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            val highConfidenceScore = 40f
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFingerprintPreviousStepsHavingAHighConfidence(highConfidenceScore)
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
            verify { Simber.i(any<DuplicateBiometricEnrolmentCheckFailed>()) }
        }
    }

    @Test
    fun fingerprintWithEnrolmentPlus_processRequestWithLessThanHighConfidenceInPreviousSteps_shouldSucceedWithRegistration() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            val lowerThanHighConfidenceScore = 39f
            val newEnrolment = mockk<Subject>()
            every {
                enrolHelper.buildSubject(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns newEnrolment
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFingerprintPreviousStepsHavingAHighConfidence(
                    lowerThanHighConfidenceScore
                )
            )

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.subjectId)
            }
        }
    }

    @Test
    fun faceWithEnrolmentPlus_processRequestWithHighConfidenceInPreviousSteps_shouldFail() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            val highConfidenceScore = 40f
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFacePreviousStepsHavingAHighConfidence(highConfidenceScore)
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
            verify { Simber.i(any<DuplicateBiometricEnrolmentCheckFailed>()) }
        }
    }

    @Test
    fun faceWithEnrolmentPlus_processRequestWithLessThanHighConfidenceInPreviousSteps_shouldSucceedWithRegistration() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            val lowerThanHighConfidenceScore = 39f
            val newEnrolment = mockk<Subject>()
            every {
                enrolHelper.buildSubject(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns newEnrolment
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFacePreviousStepsHavingAHighConfidence(lowerThanHighConfidenceScore)
            )

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.subjectId)
            }
        }
    }

    @Test
    fun fingerprintAndFaceWithEnrolmentPlus_processRequestWithHighConfidenceInPreviousSteps_shouldFail() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            val highConfidenceScore = 40f
            viewModel.processEnrolLastBiometricsRequest(
                EnrolLastBiometricsRequest(
                    DEFAULT_PROJECT_ID,
                    DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID,
                    buildFaceMatchStepsWithConfidence(highConfidenceScore) +
                        buildFingerprintMatchStepsWithConfidence(highConfidenceScore),
                    GUID1
                )
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
            verify { Simber.i(any<DuplicateBiometricEnrolmentCheckFailed>()) }
        }
    }

    @Test
    fun fingerprintAndFaceWithEnrolmentPlus_processRequestWithHighConfidenceInFingerprint_shouldFail() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            viewModel.processEnrolLastBiometricsRequest(
                EnrolLastBiometricsRequest(
                    DEFAULT_PROJECT_ID,
                    DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID,
                    buildFaceMatchStepsWithConfidence(10f) +
                        buildFingerprintMatchStepsWithConfidence(40f),
                    GUID1
                )
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
            verify { Simber.i(any<DuplicateBiometricEnrolmentCheckFailed>()) }
        }
    }

    @Test
    fun fingerprintAndFaceWithEnrolmentPlus_processRequestWithHighConfidenceInFace_shouldFail() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            viewModel.processEnrolLastBiometricsRequest(
                EnrolLastBiometricsRequest(
                    DEFAULT_PROJECT_ID,
                    DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID,
                    buildFaceMatchStepsWithConfidence(40f) +
                        buildFingerprintMatchStepsWithConfidence(10f),
                    GUID1
                )
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
            verify { Simber.i(any<DuplicateBiometricEnrolmentCheckFailed>()) }
        }
    }

    @Test
    fun fingerprintAndFaceWithEnrolmentPlus_processRequestWithNoHighConfidence_shouldSucceedWithRegistration() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            viewModel.processEnrolLastBiometricsRequest(
                EnrolLastBiometricsRequest(
                    DEFAULT_PROJECT_ID,
                    DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID,
                    buildFaceMatchStepsWithConfidence(10f) +
                        buildFingerprintMatchStepsWithConfidence(10f),
                    GUID1
                )
            )
            val newEnrolment = mockk<Subject>()
            every {
                enrolHelper.buildSubject(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns newEnrolment

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.subjectId)
            }
        }
    }

    @Test
    fun requestWithNeitherFingerprintNorFaceMatchResponse_shouldFail() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns true
            viewModel.processEnrolLastBiometricsRequest(
                EnrolLastBiometricsRequest(
                    DEFAULT_PROJECT_ID,
                    DEFAULT_USER_ID,
                    DEFAULT_MODULE_ID,
                    listOf(),
                    GUID1
                )
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
            verify { Simber.e(any<MissingCaptureResponse>()) }
        }
    }

    @Test
    fun exceptionDuringEnrolment_shouldFail() {
        runTest {
            every { generalConfiguration.duplicateBiometricEnrolmentCheck } returns false
            val exception = EnrolmentEventValidatorException()
            coEvery { enrolHelper.enrol(any()) } throws exception
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFacePreviousStepsHavingAHighConfidence(30f)
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
            verify { Simber.e(exception) }
        }
    }

    private fun buildRequestWithFingerprintPreviousStepsHavingAHighConfidence(confidence: Float) =
        EnrolLastBiometricsRequest(
            DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, buildFingerprintMatchStepsWithConfidence(confidence), GUID1
        )

    private fun buildFingerprintMatchStepsWithConfidence(confidence: Float) = listOf(
        Step(
            requestCode = 234,
            activityName = "com.simprints.id.MyFingerprintActivity",
            bundleKey = "BUNDLE_KEY",
            request = mockk(),
            result = FingerprintMatchResponse(
                listOf(
                    FingerprintMatchResult("person_id", confidence)
                )
            ),
            status = COMPLETED
        )
    )

    private fun buildRequestWithFacePreviousStepsHavingAHighConfidence(confidence: Float) =
        EnrolLastBiometricsRequest(
            DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, buildFaceMatchStepsWithConfidence(confidence), GUID1
        )

    private fun buildFaceMatchStepsWithConfidence(confidence: Float) = listOf(
        Step(
            requestCode = 322,
            activityName = "com.simprints.id.MyFaceActivity",
            bundleKey = "BUNDLE_KEY",
            request = mockk(),
            result = FaceMatchResponse(
                listOf(
                    FaceMatchResult(guidFound = "person_id", confidence = confidence)
                )
            ),
            status = COMPLETED
        )
    )
}

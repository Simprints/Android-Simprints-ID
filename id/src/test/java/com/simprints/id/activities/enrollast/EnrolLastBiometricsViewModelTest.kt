package com.simprints.id.activities.enrollast

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Failed
import com.simprints.id.activities.enrollast.EnrolLastBiometricsActivity.ViewState.Success
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.domain.moduleapi.face.responses.FaceMatchResponse
import com.simprints.id.domain.moduleapi.face.responses.entities.FaceMatchResult
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintMatchResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.entities.FingerprintMatchResult
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import com.simprints.id.orchestrator.steps.core.requests.EnrolLastBiometricsRequest
import com.simprints.id.orchestrator.steps.core.response.EnrolLastBiometricsResponse
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EnrolLastBiometricsViewModelTest {

    companion object {
        private val fingerprintConfidenceThresholds = mapOf(
            FingerprintConfidenceThresholds.LOW to 15,
            FingerprintConfidenceThresholds.MEDIUM to 30,
            FingerprintConfidenceThresholds.HIGH to 40
        )
        private val faceConfidenceThresholds = mapOf(
            FaceConfidenceThresholds.LOW to 15,
            FaceConfidenceThresholds.MEDIUM to 30,
            FaceConfidenceThresholds.HIGH to 40
        )
    }

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    lateinit var viewModel: EnrolLastBiometricsViewModel
    private val stepsWithLastEnrolBiometrics = listOf(
        Step(GUID1, 0, "activity_name", "key", mockk<EnrolLastBiometricsRequest>(), EnrolLastBiometricsResponse(GUID1), COMPLETED))

    private val appRequestWithPastEnrolLastBiometricSteps = EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, stepsWithLastEnrolBiometrics, GUID1)
    private val appRequestWithoutPastEnrolLastBiometricSteps = EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, emptyList(), GUID1)

    @MockK lateinit var timeHelper: TimeHelper
    @MockK lateinit var enrolHelper: EnrolmentHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun getNextStep_enrolFails_shouldProduceFailedState() {
        runBlocking {
            buildViewModel()
            val request = mockk<EnrolLastBiometricsRequest>()
            every { request.previousSteps } throws Throwable("No steps from previous run")
            viewModel.processEnrolLastBiometricsRequest(request)
            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
        }
    }

    @Test
    fun getNextStep_enrolAlreadyHappened_shouldProduceSuccessState() {
        runBlocking {
            buildViewModel()
            viewModel.processEnrolLastBiometricsRequest(appRequestWithPastEnrolLastBiometricSteps)

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(GUID1)
            }
        }
    }

    @Test
    fun getNextStep_enrolNeverHappened_shouldProduceSuccessState() {
        runBlocking {
            buildViewModel()
            val newEnrolment = SubjectsGeneratorUtils.getRandomSubject()
            every { enrolHelper.buildSubject(any(), any(), any(), any(), any(), any()) } returns newEnrolment

            viewModel.processEnrolLastBiometricsRequest(appRequestWithoutPastEnrolLastBiometricSteps)

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.subjectId)
            }
        }
    }

    @Test
    fun fingerprintWithEnrolmentPlus_processRequestWithHighConfidenceInPreviousSteps_shouldFail() {
        runBlocking {
            buildViewModel(isEnrolmentPlus = true)
            val highConfidenceScore = 40f
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFingerprintPreviousStepsHavingAHighConfidence(highConfidenceScore)
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
        }
    }

    @Test
    fun fingerprintWithEnrolmentPlus_processRequestWithLessThanHighConfidenceInPreviousSteps_shouldSucceedWithRegistration() {
        runBlocking {
            buildViewModel(isEnrolmentPlus = true)
            val lowerThanHighConfidenceScore = 39f
            val newEnrolment = SubjectsGeneratorUtils.getRandomSubject()
            every { enrolHelper.buildSubject(any(), any(), any(), any(), any(), any()) } returns newEnrolment
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFingerprintPreviousStepsHavingAHighConfidence(lowerThanHighConfidenceScore)
            )

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.subjectId)
            }
        }
    }

    @Test
    fun faceWithEnrolmentPlus_processRequestWithHighConfidenceInPreviousSteps_shouldFail() {
        runBlocking {
            buildViewModel(isEnrolmentPlus = true)
            val highConfidenceScore = 40f
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFacePreviousStepsHavingAHighConfidence(highConfidenceScore)
            )

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(Failed)
        }
    }

    @Test
    fun faceWithEnrolmentPlus_processRequestWithLessThanHighConfidenceInPreviousSteps_shouldSucceedWithRegistration() {
        runBlocking {
            buildViewModel(isEnrolmentPlus = true)
            val lowerThanHighConfidenceScore = 39f
            val newEnrolment = SubjectsGeneratorUtils.getRandomSubject()
            every { enrolHelper.buildSubject(any(), any(), any(), any(), any(), any()) } returns newEnrolment
            viewModel.processEnrolLastBiometricsRequest(
                buildRequestWithFacePreviousStepsHavingAHighConfidence(lowerThanHighConfidenceScore)
            )

            with(viewModel.getViewStateLiveData()) {
                assertThat(this.value).isInstanceOf(Success::class.java)
                assertThat((this.value as Success).newGuid).isEqualTo(newEnrolment.subjectId)
            }
        }
    }

    private fun buildViewModel(isEnrolmentPlus: Boolean = false) {
        viewModel = EnrolLastBiometricsViewModel(enrolHelper, timeHelper,
            fingerprintConfidenceThresholds, faceConfidenceThresholds, isEnrolmentPlus)
    }

    private fun buildRequestWithFingerprintPreviousStepsHavingAHighConfidence(confidence: Float) =
        EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, buildFingerprintMatchStepsWithConfidence(confidence), GUID1)

    private fun buildFingerprintMatchStepsWithConfidence(confidence: Float) = listOf(
        Step(
            requestCode = 234,
            activityName = "com.simprints.id.MyFingerprintActivity",
            bundleKey = "BUNDLE_KEY",
            request = mockk(),
            result = FingerprintMatchResponse(listOf(
                FingerprintMatchResult("person_id", confidence)
            )),
            status = COMPLETED
        )
    )

    private fun buildRequestWithFacePreviousStepsHavingAHighConfidence(confidence: Float) =
        EnrolLastBiometricsRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID,
            DEFAULT_MODULE_ID, buildFaceMatchStepsWithConfidence(confidence), GUID1)

    private fun buildFaceMatchStepsWithConfidence(confidence: Float) = listOf(
        Step(
            requestCode = 322,
            activityName = "com.simprints.id.MyFaceActivity",
            bundleKey = "BUNDLE_KEY",
            request = mockk(),
            result = FaceMatchResponse(listOf(
                FaceMatchResult(guidFound = "person_id", confidence = confidence)
            )),
            status = COMPLETED
        )
    )
}

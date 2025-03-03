package com.simprints.feature.orchestrator

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.exceptions.SubjectAgeNotSupportedException
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.orchestrator.usecases.AddCallbackEventUseCase
import com.simprints.feature.orchestrator.usecases.MapRefusalOrErrorResultUseCase
import com.simprints.feature.orchestrator.usecases.MapStepsForLastBiometricEnrolUseCase
import com.simprints.feature.orchestrator.usecases.UpdateDailyActivityUseCase
import com.simprints.feature.orchestrator.usecases.response.AppResponseBuilderUseCase
import com.simprints.feature.orchestrator.usecases.steps.BuildStepsUseCase
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.setup.LocationStore
import com.simprints.feature.setup.SetupResult
import com.simprints.fingerprint.capture.FingerprintCaptureContract
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.store.models.AgeGroup
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.NEC
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.matcher.MatchParams
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class OrchestratorViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var cache: OrchestratorCache

    @MockK
    private lateinit var locationStore: LocationStore

    @MockK
    private lateinit var stepsBuilder: BuildStepsUseCase

    @MockK
    private lateinit var mapRefusalOrErrorResult: MapRefusalOrErrorResultUseCase

    @MockK
    private lateinit var appResponseBuilder: AppResponseBuilderUseCase

    @MockK
    private lateinit var addCallbackEvent: AddCallbackEventUseCase

    @MockK
    private lateinit var dailyActivityUseCase: UpdateDailyActivityUseCase

    @MockK
    private lateinit var mapStepsForLastBiometricEnrolUseCase: MapStepsForLastBiometricEnrolUseCase

    private lateinit var viewModel: OrchestratorViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = OrchestratorViewModel(
            configManager,
            cache,
            locationStore,
            stepsBuilder,
            mapRefusalOrErrorResult,
            appResponseBuilder,
            addCallbackEvent,
            dailyActivityUseCase,
            mapStepsForLastBiometricEnrolUseCase,
        )
    }

    @Test
    fun `Starts executing steps when action when received`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.SETUP),
        )

        viewModel.handleAction(mockk())

        viewModel.currentStep.test().assertValue { step ->
            step.peekContent()?.let { it.id == StepId.SETUP && it.status == StepStatus.IN_PROGRESS }
        }
    }

    @Test
    fun `Executes next steps after step result`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.SETUP),
            createMockStep(StepId.CONSENT),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        val stepsObserver = viewModel.currentStep.test()

        viewModel.handleAction(mockk())
        viewModel.handleResult(SetupResult(true))

        assertThat(stepsObserver.valueHistory().mapNotNull { it.peekContent()?.id })
            .isEqualTo(listOf(StepId.SETUP, StepId.CONSENT))
    }

    @Test
    fun `Returns response when all steps executed`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.SETUP),
            createMockStep(StepId.CONSENT),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        coEvery { appResponseBuilder(any(), any(), any(), any()) } returns mockk()
        coJustRun { dailyActivityUseCase(any()) }
        justRun { addCallbackEvent(any()) }

        viewModel.handleAction(mockk())
        viewModel.handleResult(SetupResult(true))
        viewModel.handleResult(ConsentResult(true))

        viewModel.appResponse.test().assertHasValue()
    }

    @Test
    fun `Returns response when error result received`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.SETUP),
            createMockStep(StepId.CONSENT),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)

        viewModel.handleAction(mockk())
        viewModel.handleResult(SetupResult(true))

        viewModel.appResponse.test().assertHasValue()
    }

    @Test
    fun `Returns AGE_GROUP_NOT_SUPPORTED response when step builder throws SubjectAgeNotSupportedException`() = runTest {
        every { stepsBuilder.build(any(), any()) } throws SubjectAgeNotSupportedException()

        viewModel.handleAction(mockk())

        val expectedResponse = AppErrorResponse(AppErrorReason.AGE_GROUP_NOT_SUPPORTED)
        verify { addCallbackEvent(expectedResponse) }
        viewModel.appResponse.test().value().peekContent().let { response ->
            assertThat(response.response).isEqualTo(expectedResponse)
        }
    }

    @Test
    fun `Appends capture and match steps upon receiving SelectSubjectAgeGroupResult`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.SELECT_SUBJECT_AGE),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        val captureAndMatchSteps = listOf(
            createMockStep(StepId.FACE_CAPTURE),
            createMockStep(
                StepId.FACE_MATCHER,
                MatchStepStubPayload.asBundle(
                    FlowType.VERIFY,
                    SubjectQuery(),
                    BiometricDataSource.Simprints,
                ),
            ),
        )
        every { stepsBuilder.buildCaptureAndMatchStepsForAgeGroup(any(), any(), any()) } returns captureAndMatchSteps

        viewModel.handleAction(mockk())
        viewModel.handleResult(SelectSubjectAgeGroupResult(AgeGroup(0, 1)))

        verify { stepsBuilder.buildCaptureAndMatchStepsForAgeGroup(any(), any(), any()) }
        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FACE_CAPTURE)
        }
    }

    @Test
    fun `Updates face matcher step payload when receiving face capture`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.FACE_CAPTURE),
            createMockStep(
                StepId.FACE_MATCHER,
                MatchStepStubPayload.asBundle(
                    FlowType.VERIFY,
                    SubjectQuery(),
                    BiometricDataSource.Simprints,
                ),
            ),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(FaceCaptureResult("", emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FACE_MATCHER)
        }
    }

    @Test
    fun `Updates fingerprint matcher step payload when receiving fingerprint capture`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.asBundle(
                    FlowType.VERIFY,
                    SubjectQuery(),
                    BiometricDataSource.Simprints,
                ),
            ),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(FingerprintCaptureResult("", emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FINGERPRINT_MATCHER)
        }
    }

    @Test
    fun `Updates the correct fingerprint match step when multiple fingerprint SDKs are used`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(
                StepId.FINGERPRINT_CAPTURE,
                FingerprintCaptureContract.getArgs(
                    flowType = FlowType.VERIFY,
                    fingers = emptyList(),
                    fingerprintSDK = SECUGEN_SIM_MATCHER,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.asBundle(
                    flowType = FlowType.VERIFY,
                    subjectQuery = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                    fingerprintSDK = SECUGEN_SIM_MATCHER,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_CAPTURE,
                FingerprintCaptureContract.getArgs(
                    flowType = FlowType.VERIFY,
                    fingers = emptyList(),
                    fingerprintSDK = NEC,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.asBundle(
                    flowType = FlowType.VERIFY,
                    subjectQuery = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                    fingerprintSDK = NEC,
                ),
            ),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        val format = "SimMatcher"
        val sample1 = FingerprintCaptureResult.Sample(
            IFingerIdentifier.LEFT_INDEX_FINGER,
            ByteArray(0),
            0,
            null,
            format,
        )
        val sample2 = FingerprintCaptureResult.Sample(
            IFingerIdentifier.LEFT_THUMB,
            ByteArray(0),
            0,
            null,
            format,
        )
        val captureResults: List<FingerprintCaptureResult.Item> = listOf(
            FingerprintCaptureResult.Item(null, IFingerIdentifier.LEFT_INDEX_FINGER, sample1),
            FingerprintCaptureResult.Item(null, IFingerIdentifier.LEFT_THUMB, sample2),
        )

        viewModel.handleAction(mockk())
        viewModel.handleResult(FingerprintCaptureResult("", captureResults))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FINGERPRINT_MATCHER)
            val params = step.payload.getParcelable<MatchParams>("params")
            assertThat(params).isNotNull()
            assertThat(params?.fingerprintSDK).isEqualTo(SECUGEN_SIM_MATCHER)
            assertThat(params?.probeFingerprintSamples?.size).isEqualTo(2)
            assertThat(params?.probeFingerprintSamples?.get(0)?.format).isEqualTo(format)
            assertThat(params?.probeFingerprintSamples?.get(1)?.format).isEqualTo(format)
        }
    }

    @Test
    fun `Restores steps if empty`() = runTest {
        val savedSteps = listOf(
            createMockStep(StepId.SETUP),
            createMockStep(StepId.CONSENT),
        )
        every { cache.steps } returns savedSteps

        viewModel.restoreStepsIfNeeded()

        verify { cache.steps }
    }

    @Test
    fun `Does not restore steps if not empty`() = runTest {
        val originalSteps = listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
        )
        every { stepsBuilder.build(any(), any()) } returns originalSteps
        val savedSteps = listOf(
            createMockStep(StepId.SETUP),
            createMockStep(StepId.CONSENT),
        )
        every { cache.steps } returns savedSteps

        viewModel.handleAction(mockk())
        // Clear previous interactions with cache resulting from handleAction()
        clearMocks(cache, answers = false)
        viewModel.restoreStepsIfNeeded()

        verify(exactly = 0) { cache.steps }
    }

    @Test
    fun `Restores modalities if empty`() = runTest {
        val projectModalities = listOf<GeneralConfiguration.Modality>(
            mockk(),
            mockk(),
        )
        val id = "projectId"
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { projectId } returns id
            every { general.modalities } returns emptyList() andThen projectModalities
        }
        coEvery { configManager.getProject(id) } returns mockk()

        viewModel.handleAction(mockk())
        viewModel.restoreModalitiesIfNeeded()

        coVerify(exactly = 3) { configManager.getProjectConfiguration() }
    }

    @Test
    fun `Does not restore modalities if not empty`() = runTest {
        val projectModalities = listOf<GeneralConfiguration.Modality>(
            mockk(),
            mockk(),
        )
        val id = "projectId"
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { projectId } returns id
            every { general.modalities } returns projectModalities
        }
        coEvery { configManager.getProject(id) } returns mockk()

        viewModel.handleAction(mockk())
        viewModel.restoreModalitiesIfNeeded()

        coVerify(exactly = 2) { configManager.getProjectConfiguration() }
    }

    @Test
    fun `Adds new steps to Enrol Last Biometric params`() = runTest {
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        val captureStep = createMockStep(StepId.FINGERPRINT_CAPTURE)
        val enrolLastStep = createMockStep(StepId.ENROL_LAST_BIOMETRIC)
        enrolLastStep.payload.putParcelable(
            "params",
            EnrolLastBiometricParams(
                "projectId",
                TokenizableString.Tokenized("userId"),
                TokenizableString.Tokenized("moduleId"),
                listOf(mockk<EnrolLastBiometricStepResult>()),
            ),
        )
        every { stepsBuilder.build(any(), any()) } returns listOf(
            captureStep,
            enrolLastStep,
        )
        val mockEnrolLastStep = mockk<EnrolLastBiometricStepResult>()
        coEvery { mapStepsForLastBiometricEnrolUseCase(any()) } returns listOf(
            mockEnrolLastStep,
        )

        viewModel.handleAction(mockk())
        viewModel.handleResult(FingerprintCaptureResult("", emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.payload.getParcelable<EnrolLastBiometricParams>("params")?.steps)
                .containsExactly(mockEnrolLastStep)
        }
    }

    private fun createMockStep(
        stepId: Int,
        payload: Bundle = Bundle(),
    ) = Step(
        id = stepId,
        navigationActionId = 0,
        destinationId = 0,
        payload = payload,
        status = StepStatus.NOT_STARTED,
        result = null,
    )
}

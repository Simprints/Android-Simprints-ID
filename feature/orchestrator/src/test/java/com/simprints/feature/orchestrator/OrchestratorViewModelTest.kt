package com.simprints.feature.orchestrator

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.face.capture.FaceCaptureResult
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.orchestrator.usecases.AddCallbackEventUseCase
import com.simprints.feature.orchestrator.usecases.CreatePersonEventUseCase
import com.simprints.feature.orchestrator.usecases.MapRefusalOrErrorResultUseCase
import com.simprints.feature.orchestrator.usecases.ShouldCreatePersonUseCase
import com.simprints.feature.orchestrator.usecases.UpdateDailyActivityUseCase
import com.simprints.feature.orchestrator.usecases.response.AppResponseBuilderUseCase
import com.simprints.feature.orchestrator.usecases.steps.BuildStepsUseCase
import com.simprints.feature.setup.LocationStore
import com.simprints.feature.setup.SetupResult
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
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
    private lateinit var shouldCreatePerson: ShouldCreatePersonUseCase

    @MockK
    private lateinit var createPersonEvent: CreatePersonEventUseCase

    @MockK
    private lateinit var appResponseBuilder: AppResponseBuilderUseCase

    @MockK
    private lateinit var addCallbackEvent: AddCallbackEventUseCase

    @MockK
    private lateinit var dailyActivityUseCase: UpdateDailyActivityUseCase


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
            shouldCreatePerson,
            createPersonEvent,
            appResponseBuilder,
            addCallbackEvent,
            dailyActivityUseCase,
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
        every { mapRefusalOrErrorResult(any()) } returns null
        every { shouldCreatePerson(any(), any(), any()) } returns false

        val stepsObserver = viewModel.currentStep.test()

        viewModel.handleAction(mockk())
        viewModel.handleResult(SetupResult(true))

        assertThat(stepsObserver.valueHistory().mapNotNull { it.peekContent()?.id })
            .isEqualTo(listOf(StepId.SETUP, StepId.CONSENT))
    }

    @Test
    fun `Creates person if required after step result`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns emptyList()
        every { mapRefusalOrErrorResult(any()) } returns null

        every { shouldCreatePerson(any(), any(), any()) } returns true
        coJustRun { createPersonEvent(any()) }

        viewModel.handleResult(SetupResult(true))

        coVerify { createPersonEvent(any()) }
    }

    @Test
    fun `Returns response when all steps executed`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.SETUP),
            createMockStep(StepId.CONSENT),
        )
        every { mapRefusalOrErrorResult(any()) } returns null
        every { shouldCreatePerson(any(), any(), any()) } returns false
        coEvery { appResponseBuilder(any(), any(), any()) } returns mockk()
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
        every { mapRefusalOrErrorResult(any()) } returns AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR)

        viewModel.handleAction(mockk())
        viewModel.handleResult(SetupResult(true))

        viewModel.appResponse.test().assertHasValue()
    }

    @Test
    fun `Updates face matcher step payload when receiving face capture`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.FACE_CAPTURE),
            createMockStep(StepId.FACE_MATCHER, MatchStepStubPayload.asBundle(FlowType.VERIFY, SubjectQuery())),
        )
        every { mapRefusalOrErrorResult(any()) } returns null
        every { shouldCreatePerson(any(), any(), any()) } returns false

        viewModel.handleAction(mockk())
        viewModel.handleResult(FaceCaptureResult(emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FACE_MATCHER)
        }
    }

    @Test
    fun `Updates fingerprint matcher step payload when receiving fingerprint capture`() = runTest {
        every { stepsBuilder.build(any(), any()) } returns listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
            createMockStep(StepId.FINGERPRINT_MATCHER, MatchStepStubPayload.asBundle(FlowType.VERIFY, SubjectQuery())),
        )
        every { mapRefusalOrErrorResult(any()) } returns null
        every { shouldCreatePerson(any(), any(), any()) } returns false

        viewModel.handleAction(mockk())
        viewModel.handleResult(FingerprintCaptureResult(emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FINGERPRINT_MATCHER)
        }
    }

    private fun createMockStep(stepId: Int, payload: Bundle = Bundle()) = Step(
        id = stepId,
        navigationActionId = 0,
        destinationId = 0,
        payload = payload,
        status = StepStatus.NOT_STARTED,
        result = null
    )
}

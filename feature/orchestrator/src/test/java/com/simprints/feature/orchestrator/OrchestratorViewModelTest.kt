package com.simprints.feature.orchestrator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.capture.BiometricReferenceCapture
import com.simprints.core.domain.capture.BiometricTemplateCapture
import com.simprints.core.domain.common.AgeGroup
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.consent.ConsentResult
import com.simprints.feature.enrollast.EnrolLastBiometricParams
import com.simprints.feature.enrollast.EnrolLastBiometricStepResult
import com.simprints.feature.externalcredential.ExternalCredentialSearchResult
import com.simprints.feature.externalcredential.model.ExternalCredentialParams
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.feature.orchestrator.exceptions.SubjectAgeNotSupportedException
import com.simprints.feature.orchestrator.steps.MatchStepStubPayload
import com.simprints.feature.orchestrator.steps.Step
import com.simprints.feature.orchestrator.steps.StepId
import com.simprints.feature.orchestrator.steps.StepStatus
import com.simprints.feature.orchestrator.tools.OrchestrationJsonHelper
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
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.GUID2
import com.simprints.infra.matching.MatchParams
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.DefaultAsserter.fail

@RunWith(AndroidJUnit4::class)
internal class OrchestratorViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configRepository: ConfigRepository

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
            configRepository,
            cache,
            locationStore,
            stepsBuilder,
            mapRefusalOrErrorResult,
            appResponseBuilder,
            addCallbackEvent,
            dailyActivityUseCase,
            mapStepsForLastBiometricEnrolUseCase,
            OrchestrationJsonHelper(),
        )
    }

    @Test
    fun `Starts executing steps when action when received`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.SETUP),
        )

        viewModel.handleAction(mockk())

        viewModel.currentStep.test().assertValue { step ->
            step.peekContent()?.let { it.id == StepId.SETUP && it.status == StepStatus.IN_PROGRESS }
        }
    }

    @Test
    fun `Executes next steps after step result`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
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
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.SETUP),
            createMockStep(StepId.CONSENT),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        coEvery { appResponseBuilder(any(), any(), any(), any(), any()) } returns mockk()
        coJustRun { dailyActivityUseCase(any()) }
        justRun { addCallbackEvent(any()) }

        viewModel.handleAction(mockk())
        viewModel.handleResult(SetupResult(true))
        viewModel.handleResult(ConsentResult(true))

        viewModel.appResponse.test().assertHasValue()
    }

    @Test
    fun `Returns response when error result received`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
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
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } throws SubjectAgeNotSupportedException()

        viewModel.handleAction(mockk())

        val expectedResponse = AppErrorResponse(AppErrorReason.AGE_GROUP_NOT_SUPPORTED)
        verify { addCallbackEvent(expectedResponse) }
        viewModel.appResponse.test().value().peekContent().let { response ->
            assertThat(response.response).isEqualTo(expectedResponse)
        }
    }

    @Test
    fun `Appends capture and match steps upon receiving SelectSubjectAgeGroupResult`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.SELECT_SUBJECT_AGE),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        val captureAndMatchSteps = listOf(
            createMockStep(StepId.FACE_CAPTURE),
            createMockStep(
                StepId.FACE_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
                    FlowType.VERIFY,
                    EnrolmentRecordQuery(),
                    BiometricDataSource.Simprints,
                    ModalitySdkType.RANK_ONE,
                ),
            ),
        )
        coEvery { stepsBuilder.buildCaptureAndMatchStepsForAgeGroup(any(), any(), any(), any()) } returns captureAndMatchSteps

        viewModel.handleAction(mockk())
        viewModel.handleResult(SelectSubjectAgeGroupResult(AgeGroup(0, 1)))

        coVerify { stepsBuilder.buildCaptureAndMatchStepsForAgeGroup(any(), any(), any(), any()) }
        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FACE_CAPTURE)
        }
    }

    @Test
    fun `Updates face matcher step payload when receiving face capture`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FACE_CAPTURE),
            createMockStep(
                StepId.FACE_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
                    FlowType.VERIFY,
                    EnrolmentRecordQuery(),
                    BiometricDataSource.Simprints,
                    ModalitySdkType.RANK_ONE,
                ),
            ),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(BiometricReferenceCapture("", Modality.FACE, "format", emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FACE_MATCHER)
        }
    }

    @Test
    fun `Updates fingerprint matcher step payload when receiving fingerprint capture`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
                    FlowType.VERIFY,
                    EnrolmentRecordQuery(),
                    BiometricDataSource.Simprints,
                    ModalitySdkType.RANK_ONE,
                ),
            ),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(BiometricReferenceCapture("", Modality.FINGERPRINT, "format", emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FINGERPRINT_MATCHER)
        }
    }

    @Test
    fun `Updates the correct fingerprint match step when multiple fingerprint SDKs are used`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(
                StepId.FINGERPRINT_CAPTURE,
                FingerprintCaptureContract.getParams(
                    flowType = FlowType.VERIFY,
                    fingers = emptyList(),
                    fingerprintSDK = ModalitySdkType.SECUGEN_SIM_MATCHER,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
                    flowType = FlowType.VERIFY,
                    enrolmentRecordQuery = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                    bioSdk = ModalitySdkType.SECUGEN_SIM_MATCHER,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_CAPTURE,
                FingerprintCaptureContract.getParams(
                    flowType = FlowType.VERIFY,
                    fingers = emptyList(),
                    fingerprintSDK = ModalitySdkType.NEC,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
                    flowType = FlowType.VERIFY,
                    enrolmentRecordQuery = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                    bioSdk = ModalitySdkType.NEC,
                ),
            ),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        val format = "SimMatcher"
        val capture1 = BiometricTemplateCapture(
            captureEventId = GUID1,
            identifier = TemplateIdentifier.LEFT_INDEX_FINGER,
            template = ByteArray(0),
        )
        val capture2 = BiometricTemplateCapture(
            captureEventId = GUID2,
            identifier = TemplateIdentifier.LEFT_THUMB,
            template = ByteArray(0),
        )

        viewModel.handleAction(mockk())
        viewModel.handleResult(BiometricReferenceCapture("", Modality.FINGERPRINT, format, listOf(capture1, capture2)))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isEqualTo(StepId.FINGERPRINT_MATCHER)
            val params = step.params?.let { it as? MatchParams }
            assertThat(params).isNotNull()
            assertThat(params?.bioSdk).isEqualTo(ModalitySdkType.SECUGEN_SIM_MATCHER)
            assertThat(params?.probeReference?.templates?.size).isEqualTo(2)
            assertThat(params?.probeReference?.format).isEqualTo(format)
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
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns originalSteps
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
        val projectModalities = listOf<Modality>(
            mockk(),
            mockk(),
        )
        val id = "projectId"
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { projectId } returns id
            every { general.modalities } returns emptyList() andThen projectModalities
        }
        coEvery { configRepository.getProject() } returns mockk()

        viewModel.handleAction(mockk())
        viewModel.restoreModalitiesIfNeeded()

        coVerify(exactly = 3) { configRepository.getProjectConfiguration() }
    }

    @Test
    fun `Does not restore modalities if not empty`() = runTest {
        val projectModalities = listOf<Modality>(
            mockk(),
            mockk(),
        )
        val id = "projectId"
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { projectId } returns id
            every { general.modalities } returns projectModalities
        }
        coEvery { configRepository.getProject() } returns mockk()

        viewModel.handleAction(mockk())
        viewModel.restoreModalitiesIfNeeded()

        coVerify(exactly = 2) { configRepository.getProjectConfiguration() }
    }

    @Test
    fun `Adds new steps to Enrol Last Biometric params`() = runTest {
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        val captureStep = createMockStep(StepId.FINGERPRINT_CAPTURE)
        val enrolLastStep = createMockStep(StepId.ENROL_LAST_BIOMETRIC)
        enrolLastStep.params = EnrolLastBiometricParams(
            projectId = "projectId",
            userId = TokenizableString.Tokenized("userId"),
            moduleId = TokenizableString.Tokenized("moduleId"),
            steps = listOf(mockk<EnrolLastBiometricStepResult>()),
            scannedCredential = null,
        )
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            captureStep,
            enrolLastStep,
        )
        val mockEnrolLastStep = mockk<EnrolLastBiometricStepResult>()
        coEvery { mapStepsForLastBiometricEnrolUseCase(any()) } returns listOf(
            mockEnrolLastStep,
        )

        viewModel.handleAction(mockk())
        viewModel.handleResult(BiometricReferenceCapture("", Modality.FINGERPRINT, "format", emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.params?.let { it as? EnrolLastBiometricParams }?.steps).containsExactly(mockEnrolLastStep)
        }
    }

    @Test
    fun `Updates external credential step payload with fingerprint samples when receiving fingerprint capture result`() = runTest {
        val fingerprintReferenceId = "fingerprintReferenceId"
        val fingerId1 = TemplateIdentifier.LEFT_INDEX_FINGER
        val fingerId2 = TemplateIdentifier.RIGHT_THUMB
        val template1 = ByteArray(10)
        val template2 = ByteArray(20)
        val format1 = "format1"

        val fingerprintCapture1 = BiometricTemplateCapture(
            captureEventId = GUID1,
            identifier = fingerId1,
            template = template1,
        )
        val fingerprintCapture2 = BiometricTemplateCapture(
            captureEventId = GUID2,
            identifier = fingerId2,
            template = template2,
        )

        val externalCredentialParams = mockk<ExternalCredentialParams>(relaxed = true) {
            every { copy(probeReferences = any()) } returns this
        }

        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
            createMockStep(StepId.EXTERNAL_CREDENTIAL, externalCredentialParams),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(
            BiometricReferenceCapture(
                fingerprintReferenceId,
                Modality.FINGERPRINT,
                format1,
                listOf(fingerprintCapture1, fingerprintCapture2),
            ),
        )

        val expectedFingerprintReference =
            BiometricReferenceCapture(
                fingerprintReferenceId,
                Modality.FINGERPRINT,
                format1,
                listOf(fingerprintCapture1, fingerprintCapture2),
            )

        verify {
            externalCredentialParams.copy(
                probeReferences = listOf(expectedFingerprintReference),
            )
        }
    }

    @Test
    fun `Removes matcher steps when external credential search has good matches in identify flow`() = runTest {
        val externalCredentialResult = mockk<ExternalCredentialSearchResult> {
            every { flowType } returns FlowType.IDENTIFY
            every { goodMatches } returns listOf(mockk())
        }

        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FACE_CAPTURE),
            createMockStep(StepId.FINGERPRINT_CAPTURE),
            createMockStep(StepId.FACE_MATCHER),
            createMockStep(StepId.FINGERPRINT_MATCHER),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(externalCredentialResult)

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.id).isNotEqualTo(StepId.FACE_MATCHER)
            assertThat(step.id).isNotEqualTo(StepId.FINGERPRINT_MATCHER)
        }
    }

    @Test
    fun `Does not remove matcher steps when flow type is enrol even with good matches`() = runTest {
        val externalCredentialResult = mockk<ExternalCredentialSearchResult> {
            every { flowType } returns FlowType.ENROL
            every { goodMatches } returns listOf(mockk())
        }

        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FACE_CAPTURE),
            createMockStep(StepId.FACE_MATCHER),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(externalCredentialResult)

        val stepsObserver = viewModel.currentStep.test()
        val allStepIds = stepsObserver.valueHistory().mapNotNull { it.peekContent()?.id }

        assertThat(allStepIds).contains(StepId.FACE_MATCHER)
    }

    @Test
    fun `Passes cached scanned credential to steps builder when external credential step exists in cache`() = runTest {
        val mockScannedCredential = mockk<ScannedCredential>(relaxed = true)
        val externalCredentialResult = mockk<ExternalCredentialSearchResult> {
            every { scannedCredential } returns mockScannedCredential
        }

        val externalCredentialStep = createMockStep(StepId.EXTERNAL_CREDENTIAL).apply {
            status = StepStatus.COMPLETED
            result = externalCredentialResult
        }

        val cachedSteps = listOf(
            createMockStep(StepId.SETUP).apply { status = StepStatus.COMPLETED },
            externalCredentialStep,
        )

        every { cache.steps } returns cachedSteps
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.CONFIRM_IDENTITY),
        )

        viewModel.handleAction(mockk(relaxed = true))

        coVerify {
            stepsBuilder.build(
                action = any(),
                projectConfiguration = any(),
                enrolmentSubjectId = any(),
                cachedScannedCredential = mockScannedCredential,
            )
        }
    }

    @Test
    fun `Cancels location collection when all steps complete`() = runTest {
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.SETUP).apply { status = StepStatus.IN_PROGRESS },
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null
        coEvery { appResponseBuilder(any(), any(), any(), any(), any()) } returns mockk()
        coJustRun { dailyActivityUseCase(any()) }
        justRun { addCallbackEvent(any()) }
        justRun { locationStore.cancelLocationCollection() }

        viewModel.handleAction(mockk())
        viewModel.handleResult(SetupResult(true))

        verify(exactly = 1) { locationStore.cancelLocationCollection() }
    }

    @Test
    fun `Combines cached steps with newly built steps in handleAction`() = runTest {
        // 1. Cached step is already completed
        val cachedStep = createMockStep(StepId.SETUP).apply { status = StepStatus.COMPLETED }
        // 2. New step is ready to start
        val newStep = createMockStep(StepId.CONSENT)

        every { cache.steps } returns listOf(cachedStep)
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(newStep)

        viewModel.handleAction(mockk())

        // The ViewModel should skip the completed cached step and serve the new step
        viewModel.currentStep.test().assertValue {
            it.peekContent()?.id == StepId.CONSENT
        }

        verify(exactly = 1) { cache.steps }
    }

    @Test
    fun `Ignores external credential update if external credential step is missing`() = runTest {
        // Setup: A flow that captures biometrics but DOES NOT have an External Credential step
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())

        // Action: Return a result that would normally trigger an update
        val bioResult = BiometricReferenceCapture("ref", Modality.FINGERPRINT, "fmt", emptyList())

        // Assert: execution completes without exception
        try {
            viewModel.handleResult(bioResult)
        } catch (_: Exception) {
            fail("Should not throw exception when External Credential step is missing")
        }

        // Verify we proceeded (flow finished in this case as it was the only step)
        viewModel.appResponse.test().assertHasValue()
    }

    @Test
    fun `Ignores matcher update if matcher step is missing`() = runTest {
        // Setup: A flow with Capture but NO Matcher
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FACE_CAPTURE),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())

        // Action: Return a capture result
        val bioResult = BiometricReferenceCapture("ref", Modality.FACE, "fmt", emptyList())

        // Assert: execution completes without exception
        try {
            viewModel.handleResult(bioResult)
        } catch (_: Exception) {
            fail("Should not throw exception when Matcher step is missing")
        }
    }

    @Test
    fun `Handles malformed JSON in setActionRequestFromJson gracefully`() {
        // Should catch exception internally and log it, not crash
        try {
            viewModel.setActionRequestFromJson("{ invalid_json }")
        } catch (_: Exception) {
            fail("ViewModel should catch JSON deserialization exceptions internally")
        }
    }

    private fun createMockStep(
        stepId: Int,
        params: StepParams? = null,
    ) = Step(
        id = stepId,
        navigationActionId = 0,
        destinationId = 0,
        params = params,
        status = StepStatus.NOT_STARTED,
        result = null,
    )
}

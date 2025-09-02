package com.simprints.feature.orchestrator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.step.StepParams
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.face.capture.FaceCaptureResult
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
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
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
                    SubjectQuery(),
                    BiometricDataSource.Simprints,
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
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
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
        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(
                StepId.FINGERPRINT_CAPTURE,
                FingerprintCaptureContract.getParams(
                    flowType = FlowType.VERIFY,
                    fingers = emptyList(),
                    fingerprintSDK = SECUGEN_SIM_MATCHER,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
                    flowType = FlowType.VERIFY,
                    subjectQuery = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                    fingerprintSDK = SECUGEN_SIM_MATCHER,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_CAPTURE,
                FingerprintCaptureContract.getParams(
                    flowType = FlowType.VERIFY,
                    fingers = emptyList(),
                    fingerprintSDK = NEC,
                ),
            ),
            createMockStep(
                StepId.FINGERPRINT_MATCHER,
                MatchStepStubPayload.getMatchStubParams(
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
            val params = step.params?.let { it as? MatchParams }
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
        val projectModalities = listOf<Modality>(
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
        enrolLastStep.params = EnrolLastBiometricParams(
            "projectId",
            TokenizableString.Tokenized("userId"),
            TokenizableString.Tokenized("moduleId"),
            listOf(mockk<EnrolLastBiometricStepResult>()),
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
        viewModel.handleResult(FingerprintCaptureResult("", emptyList()))

        viewModel.currentStep.test().value().peekContent()?.let { step ->
            assertThat(step.params?.let { it as? EnrolLastBiometricParams }?.steps).containsExactly(mockEnrolLastStep)
        }
    }

    @Test
    fun `Updates external credential step payload with fingerprint samples when receiving fingerprint capture result`() = runTest {
        val fingerprintReferenceId = "fingerprintReferenceId"
        val fingerId1 = IFingerIdentifier.LEFT_INDEX_FINGER
        val fingerId2 = IFingerIdentifier.RIGHT_THUMB
        val template1 = ByteArray(10)
        val template2 = ByteArray(20)
        val format1 = "format1"
        val format2 = "format2"

        val fingerprintSample1 = mockk<FingerprintCaptureResult.Sample> {
            every { fingerIdentifier } returns fingerId1
            every { template } returns template1
            every { format } returns format1
        }
        val fingerprintSample2 = mockk<FingerprintCaptureResult.Sample> {
            every { fingerIdentifier } returns fingerId2
            every { template } returns template2
            every { format } returns format2
        }

        val fingerprintItem1 = mockk<FingerprintCaptureResult.Item> {
            every { sample } returns fingerprintSample1
        }
        val fingerprintItem2 = mockk<FingerprintCaptureResult.Item> {
            every { sample } returns fingerprintSample2
        }

        val externalCredentialParams = mockk<ExternalCredentialParams>(relaxed = true) {
            every { copy(probeReferenceId = any(), fingerprintSamples = any()) } returns this
        }

        coEvery { stepsBuilder.build(any(), any(), any(), any()) } returns listOf(
            createMockStep(StepId.FINGERPRINT_CAPTURE),
            createMockStep(StepId.EXTERNAL_CREDENTIAL, externalCredentialParams),
        )
        coEvery { mapRefusalOrErrorResult(any(), any()) } returns null

        viewModel.handleAction(mockk())
        viewModel.handleResult(
            FingerprintCaptureResult(
                fingerprintReferenceId,
                listOf(fingerprintItem1, fingerprintItem2),
            ),
        )

        val expectedFingerprintSamples = listOf(
            MatchParams.FingerprintSample(fingerId1, format1, template1),
            MatchParams.FingerprintSample(fingerId2, format2, template2),
        )

        verify {
            externalCredentialParams.copy(
                probeReferenceId = fingerprintReferenceId,
                fingerprintSamples = expectedFingerprintSamples,
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

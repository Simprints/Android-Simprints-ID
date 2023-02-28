package com.simprints.id.orchestrator

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintCaptureRequest
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.*
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.orchestrator.steps.fingerprint.FingerprintStepProcessorImpl
import com.simprints.id.services.location.STORE_USER_LOCATION_WORKER_TAG
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import splitties.init.appCtx

@RunWith(AndroidJUnit4::class)
class OrchestratorManagerImplTest {


    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var appResponseFactoryMock: AppResponseFactory

    @MockK
    private lateinit var modalityFlowMock: ModalityFlow

    @MockK
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var hotCache: HotCache

    @MockK
    private lateinit var personCreationEventHelper: PersonCreationEventHelper

    @MockK
    private lateinit var workManagerMock: WorkManager

    private lateinit var modalityFlowFactoryMock: ModalityFlowFactory
    private lateinit var orchestrator: OrchestratorManager
    private val mockSteps = mutableListOf<Step>()
    private val modalities = listOf(GeneralConfiguration.Modality.FACE)

    private val appEnrolRequest = AppEnrolRequest(
        "some_project_id",
        "some_user_id",
        "some_module_id",
        "{\"key\": \"some_metadata\"}"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkStatic(WorkManager::class)
        mockkStatic("splitties.init.AppCtxKt")
        every { appCtx } returns mockk()

        every { WorkManager.getInstance(any()) } returns workManagerMock

        Intents.init()

        mockSteps.clear()
        every { modalityFlowMock.steps } answers { mockSteps }

        every { hotCache.appRequest } returns appEnrolRequest

        orchestrator = buildOrchestratorManager()
        prepareModalFlowForFaceEnrol()

        intending(toPackage(FaceStepProcessorImpl.ACTIVITY_CLASS_NAME))
            .respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun orchestratorStarts_shouldGetFirstStepFromModalityFlow() = runTest {
        orchestrator.startFlowForEnrol(modalities)

        verifyOrchestratorGotNextStepFromModalityFlow()
    }

    @Test
    fun initialise_shouldStoreAppRequestInHotCache() = runTest {
        with(orchestrator) {
            startFlowForEnrol(modalities)
            progressWitFaceCapture()
        }

        verifyOrchestratorTriedToBuildFinalAppResponse()
        verifyOrchestratorTriedToCancelAllLocationWorkers()
    }

    @Test
    fun saveState_shouldSaveHotCache() = runTest {
        with(orchestrator) {
            startFlowForEnrol(modalities)
            saveState()
        }

        verify(exactly = 1) { hotCache.save(mockSteps) }
    }

    @Test
    fun restoreState_shouldLoadStepsFromHotCache() = runTest {
        with(orchestrator) {
            startFlowForEnrol(modalities)
            orchestrator.restoreState()
        }

        verify(exactly = 1) { hotCache.load() }
    }

    @Test
    fun modalityFlowReceivesAWrongResult_orchestratorShouldNotGoAhead() {
        runTest {
            with(orchestrator) {
                startFlowForEnrol(modalities)
                progressWitFaceCapture(WRONG_REQUEST_CODE, null)
            }

            verifyOrchestratorDidntTryToBuildFinalAppResponse()
        }
    }

    @Test
    fun orchestratorReceivesAResult_itShouldBeForwardedToModalityFlowAndMoveOn() = runTest {
        with(orchestrator) {
            startFlowForEnrol(modalities)
            progressWitFaceCapture()

            verifyOrchestratorForwardedResultsToModalityFlow()
            verifyOrchestratorGotNextStepFromModalityFlow(2)
        }
    }

    @Test
    fun `Fingerprint only - Person Creation Event should be added after fingerprint capture`() =
        runTest {
            with(orchestrator) {
                mockFingerprintWithCaptureCompleted()

                handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)

                coVerify { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
            }
        }

    @Test
    fun `Fingerprint only - Person Creation Event should not be added if fingerprint capture didn't return`() =
        runTest {
            with(orchestrator) {
                mockFingerprintWithCaptureCompleted(null)

                handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)

                coVerify(exactly = 0) { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
            }
        }

    @Test
    fun `test person creation event should not be added for followup requests`() = runTest {
        with(orchestrator) {
            mockFingerprintWithCaptureCompleted(null)

            handleIntentResult(enrolLastBiometricsRequest, REQUEST_CODE, Activity.RESULT_OK, null)

            coVerify(exactly = 0) { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
        }
    }

    @Test
    fun faceCapture_orchestratorShouldAddPersonCreation() = runTest {
        with(orchestrator) {
            mockFaceWithCaptureCompleted()

            handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)

            coVerify { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
        }
    }

    @Test
    fun `Face only - Person Creation Event should not be added if face capture didn't return`() =
        runTest {
            with(orchestrator) {
                mockFaceWithCaptureCompleted(null)

                handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)

                coVerify(exactly = 0) { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
            }
        }

    @Test
    fun `Face and Fingerprint - Person Creation Event should be if both captures are completed`() =
        runTest {
            with(orchestrator) {
                mockFaceAndFingerprintWithCaptureCompleted()

                handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)

                coVerify { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
            }
        }

    @Test
    fun `Face and Fingerprint - Person Creation Event should not be added if face capture didn't return`() =
        runTest {
            with(orchestrator) {
                mockFaceAndFingerprintWithCaptureCompleted(faceResult = null)

                handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)

                coVerify(exactly = 0) { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
            }
        }

    @Test
    fun `Face and Fingerprint - Person Creation Event should not be added if fingerprint capture didn't return`() =
        runTest {
            with(orchestrator) {
                mockFaceAndFingerprintWithCaptureCompleted(fingerprintResult = null)

                handleIntentResult(enrolAppRequest, REQUEST_CODE, Activity.RESULT_OK, null)

                coVerify(exactly = 0) { personCreationEventHelper.addPersonCreationEventIfNeeded(any()) }
            }
        }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun OrchestratorManager.mockFaceAndFingerprintWithCaptureCompleted(
        fingerprintResult: FingerprintCaptureResponse? = mockk(),
        faceResult: FaceCaptureResponse? = mockk()
    ) {
        mockSteps.clear()
        mockSteps.addFingerprintCaptureStep(fingerprintResult)
        mockSteps.addFaceCaptureStep(faceResult)
        every { modalityFlowMock.steps } answers { mockSteps }
        startFlowForEnrol(
            listOf(
                GeneralConfiguration.Modality.FACE,
                GeneralConfiguration.Modality.FINGERPRINT
            )
        )
    }

    private fun OrchestratorManager.mockFingerprintWithCaptureCompleted(result: FingerprintCaptureResponse? = mockk()) {
        mockSteps.clear()
        mockSteps.addFingerprintCaptureStep(result)
        every { modalityFlowMock.steps } answers { mockSteps }
        startFlowForEnrol(listOf(GeneralConfiguration.Modality.FINGERPRINT))
    }

    private fun OrchestratorManager.mockFaceWithCaptureCompleted(result: FaceCaptureResponse? = mockk()) {
        mockSteps.clear()
        mockSteps.addFaceCaptureStep(result)
        every { modalityFlowMock.steps } answers { mockSteps }
        startFlowForEnrol(listOf(GeneralConfiguration.Modality.FACE))
    }


    private fun verifyOrchestratorGotNextStepFromModalityFlow(nTimes: Int = 1) =
        verify(exactly = nTimes) { modalityFlowMock.getNextStepToLaunch() }

    private fun verifyOrchestratorForwardedResultsToModalityFlow() =
        coVerify(exactly = 1) {
            modalityFlowMock.handleIntentResult(
                eq(enrolAppRequest),
                any(),
                any(),
                any()
            )
        }

    private fun verifyOrchestratorDidntTryToBuildFinalAppResponse() =
        coVerify(exactly = 0) {
            appResponseFactoryMock.buildAppResponse(
                any(),
                any(),
                any(),
                any()
            )
        }

    private fun verifyOrchestratorTriedToBuildFinalAppResponse() =
        coVerify(exactly = 1) {
            appResponseFactoryMock.buildAppResponse(
                any(),
                any(),
                any(),
                any()
            )
        }

    private fun verifyOrchestratorTriedToCancelAllLocationWorkers() =
        verify(exactly = 1) { workManagerMock.cancelAllWorkByTag(STORE_USER_LOCATION_WORKER_TAG) }

    private fun prepareModalFlowForFaceEnrol() {
        every { modalityFlowMock.getNextStepToLaunch() } answers {
            mockSteps.firstOrNull { it.getStatus() == NOT_STARTED }
        }

        val nFaceSamplesToCapture = 3
        val request = FaceCaptureRequest(nFaceSamplesToCapture = nFaceSamplesToCapture)

        mockSteps.add(
            Step(
                requestCode = CAPTURE.value,
                activityName = FaceStepProcessorImpl.ACTIVITY_CLASS_NAME,
                bundleKey = IFaceRequest.BUNDLE_KEY,
                request = request,
                status = NOT_STARTED
            )
        )
    }

    private fun buildOrchestratorManager(): OrchestratorManager {
        modalityFlowFactoryMock = mockk<ModalityFlowFactory>().apply {
            coEvery { this@apply.createModalityFlow(any()) } returns modalityFlowMock
        }
        coEvery {
            appResponseFactoryMock.buildAppResponse(
                any(),
                any(),
                any(),
                any()
            )
        } returns mockk()
        return OrchestratorManagerImpl(
            modalityFlowFactoryMock,
            appResponseFactoryMock,
            hotCache,
            recentUserActivityManager,
            timeHelper,
            personCreationEventHelper
        )
    }

    private fun OrchestratorManager.startFlowForEnrol(
        modalities: List<GeneralConfiguration.Modality>,
        sessionId: String = ""
    ) = runTest {
        initialise(modalities, appEnrolRequest, sessionId)
        startModalityFlow()
    }

    private suspend fun OrchestratorManager.progressWitFaceCapture(
        requestCode: Int = CAPTURE.value,
        response: IFaceCaptureResponse? = IFaceCaptureResponseImpl(emptyList())
    ) {
        response?.let {
            mockSteps.firstOrNull { step ->
                step.getStatus() == ONGOING
            }?.setResult(it.fromModuleApiToDomain())
        }

        handleIntentResult(
            enrolAppRequest,
            requestCode,
            Activity.RESULT_OK,
            Intent().putExtra(IFaceResponse.BUNDLE_KEY, response)
        )
    }

    private fun MutableList<Step>.addFingerprintCaptureStep(result: Step.Result? = null) {
        add(
            Step(
                requestCode = CAPTURE.value,
                activityName = FingerprintStepProcessorImpl.ACTIVITY_CLASS_NAME,
                bundleKey = IFingerprintRequest.BUNDLE_KEY,
                request = FingerprintCaptureRequest(fingerprintsToCapture = emptyList()),
                status = COMPLETED,
                result = result
            )
        )
    }

    private fun MutableList<Step>.addFaceCaptureStep(result: Step.Result? = null) {
        add(
            Step(
                requestCode = CAPTURE.value,
                activityName = FaceStepProcessorImpl.ACTIVITY_CLASS_NAME,
                bundleKey = IFaceRequest.BUNDLE_KEY,
                request = FaceCaptureRequest(nFaceSamplesToCapture = 3),
                status = COMPLETED,
                result = result
            )
        )
    }

    companion object {
        private const val REQUEST_CODE = 0
        private const val WRONG_REQUEST_CODE = 1
    }

}

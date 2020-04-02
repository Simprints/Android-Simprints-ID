package com.simprints.id.orchestrator

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.face.requests.FaceCaptureRequest
import com.simprints.id.domain.moduleapi.face.responses.fromModuleApiToDomain
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.StepEncoder
import com.simprints.id.orchestrator.modality.ModalityFlow
import com.simprints.id.orchestrator.responsebuilders.AppResponseFactory
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Status.NOT_STARTED
import com.simprints.id.orchestrator.steps.Step.Status.ONGOING
import com.simprints.id.orchestrator.steps.face.FaceRequestCode.CAPTURE
import com.simprints.id.orchestrator.steps.face.FaceStepProcessorImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class OrchestratorManagerImplTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @MockK private lateinit var appResponseFactoryMock: AppResponseFactory
    @MockK private lateinit var modalityFlowMock: ModalityFlow
    @MockK private lateinit var dashboardDailyActivityRepositoryMock: DashboardDailyActivityRepository
    private lateinit var orchestrator: OrchestratorManager
    private val mockSteps = mutableListOf<Step>()
    private val modalities = listOf(FACE)

    private val appEnrolRequest = AppEnrolRequest(
        "some_project_id",
        "some_user_id",
        "some_module_id",
        "some_metadata"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        UnitTestConfig(this)
            .coroutinesMainThread()
            .rescheduleRxMainThread()

        Intents.init()

        mockSteps.clear()
        every { modalityFlowMock.steps } answers { mockSteps }

        orchestrator = buildOrchestratorManager()
        prepareModalFlowForFaceEnrol()

        intending(toPackage(FaceStepProcessorImpl.ACTIVITY_CLASS_NAME))
            .respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun orchestratorStarts_shouldGetFirstStepFromModalityFlow() = runBlockingTest {
        orchestrator.startFlowForEnrol(modalities)

        verifyOrchestratorGotNextStepFromModalityFlow()
    }

    @Test
    fun modalityFlowCompletes_orchestratorShouldTryToBuildAppResponse() = runBlockingTest {
        with(orchestrator) {
            startFlowForEnrol(modalities)
            progressWitFaceCapture()
        }

        verifyOrchestratorTriedToBuildFinalAppResponse()
    }

    @Test
    fun modalityFlowReceivesAWrongResult_orchestratorShouldNotGoAhead() {
        runBlocking {
            with(orchestrator) {
                startFlowForEnrol(modalities)
                progressWitFaceCapture(WRONG_REQUEST_CODE, null)
            }

            verifyOrchestratorDidntTryToBuildFinalAppResponse()
        }
    }

    @Test
    fun orchestratorReceivesAResult_itShouldBeForwardedToModalityFlowAndMoveOn() = runBlockingTest {
        with(orchestrator) {
            startFlowForEnrol(modalities)
            progressWitFaceCapture()

            verifyOrchestratorForwardedResultsToModalityFlow()
            verifyOrchestratorGotNextStepFromModalityFlow(2)
        }
    }

    @After
    fun tearDown() {
        Intents.release()
        stopKoin()
    }

    private fun verifyOrchestratorGotNextStepFromModalityFlow(nTimes: Int = 1) =
        verify(exactly = nTimes) { modalityFlowMock.getNextStepToLaunch() }

    private fun verifyOrchestratorForwardedResultsToModalityFlow() =
        coVerify(exactly = 1) { modalityFlowMock.handleIntentResult(eq(enrolAppRequest), any(), any(), any()) }

    private fun verifyOrchestratorDidntTryToBuildFinalAppResponse() =
        coVerify(exactly = 0) { appResponseFactoryMock.buildAppResponse(any(), any(), any(), any()) }

    private fun verifyOrchestratorTriedToBuildFinalAppResponse() =
        coVerify(exactly = 1) { appResponseFactoryMock.buildAppResponse(any(), any(), any(), any()) }

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
        val modalityFlowFactoryMock = mockk<ModalityFlowFactory>().apply {
            every { this@apply.createModalityFlow(any(), any()) } returns modalityFlowMock
        }
        val preferences = mockk<SharedPreferences>()
        every { preferences.edit() } returns mockk()

        val stepEncoder = mockk<StepEncoder>()
        val hotCache = HotCacheImpl(preferences, stepEncoder)
        coEvery { appResponseFactoryMock.buildAppResponse(any(), any(), any(), any()) } returns mockk()
        return OrchestratorManagerImpl(
            modalityFlowFactoryMock,
            appResponseFactoryMock,
            hotCache,
            dashboardDailyActivityRepositoryMock
        )
    }

    private fun OrchestratorManager.startFlowForEnrol(
        modalities: List<Modality>,
        sessionId: String = "") = runBlockingTest {
        initialise(modalities, appEnrolRequest, sessionId)
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
            Intent().putExtra(IFaceResponse.BUNDLE_KEY, response))
    }

    companion object {
        private const val WRONG_REQUEST_CODE = 1
    }

}
